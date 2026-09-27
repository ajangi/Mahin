package dev.mahin.backend.sync

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.security.MahinAuthSubject
import dev.mahin.backend.sync.persistence.SyncEntityRecordEntity
import dev.mahin.backend.sync.persistence.SyncEntityRecordRepository
import dev.mahin.backend.sync.persistence.SyncIdempotencyEntity
import dev.mahin.backend.sync.persistence.SyncIdempotencyRepository
import dev.mahin.backend.sync.persistence.SyncOwnerStateEntity
import dev.mahin.backend.sync.persistence.SyncOwnerStateRepository
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SyncService(
    private val syncEntityRecordRepository: SyncEntityRecordRepository,
    private val syncOwnerStateRepository: SyncOwnerStateRepository,
    private val syncIdempotencyRepository: SyncIdempotencyRepository,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun applyMutations(
        subject: MahinAuthSubject,
        request: SyncMutationRequest,
    ): SyncMutationResponse {
        val ownerKey = subject.ownerKey()
        val results = mutableListOf<SyncMutationResultItem>()
        request.mutations.forEach { mutation ->
            val cached = loadIdempotent(ownerKey, mutation)
            if (cached != null) {
                results += cached
                return@forEach
            }
            val result = applySingle(subject, ownerKey, mutation)
            storeIdempotent(ownerKey, mutation, result)
            results += result
        }
        val latest = currentRevision(ownerKey)
        return SyncMutationResponse(results = results, latestServerRevision = latest)
    }

    @Transactional(readOnly = true)
    fun pullChanges(
        subject: MahinAuthSubject,
        afterRevision: Long,
        limit: Int,
    ): SyncChangesResponse {
        val cappedLimit = limit.coerceIn(1, MAX_PULL_LIMIT)
        val records =
            when (subject) {
                is MahinAuthSubject.RegisteredUser ->
                    syncEntityRecordRepository.findUserChangesAfter(subject.userId, afterRevision)
                is MahinAuthSubject.GuestInstallation ->
                    syncEntityRecordRepository.findGuestChangesAfter(subject.guestInstallationId, afterRevision)
                is MahinAuthSubject.CmsStaff -> emptyList()
            }
        val slice = records.take(cappedLimit)
        val hasMore = records.size > cappedLimit
        val changes =
            slice.map { record ->
                SyncChangeItem(
                    entityType = record.entityType,
                    entityId = record.entityId,
                    serverRevision = record.serverRevision,
                    clientRevision = record.clientRevision,
                    updatedAt = record.updatedAt,
                    deletedAt = record.deletedAt,
                    payload = parsePayload(record.payloadJson, record.deletedAt),
                )
            }
        val latest = currentRevision(subject.ownerKey())
        return SyncChangesResponse(
            changes = changes,
            latestServerRevision = latest,
            hasMore = hasMore,
        )
    }

    @Suppress("LongMethod")
    private fun applySingle(
        subject: MahinAuthSubject,
        ownerKey: String,
        mutation: SyncMutationItem,
    ): SyncMutationResultItem {
        val existingEntity = findExisting(subject, mutation.entityType, mutation.entityId)
        val existing =
            existingEntity?.let {
                ExistingSyncEntity(
                    serverRevision = it.serverRevision,
                    updatedAt = it.updatedAt,
                    deletedAt = it.deletedAt,
                    payloadJson = it.payloadJson,
                )
            }
        val payloadJson =
            when (mutation.operation) {
                SyncMutationOperation.DELETE -> EMPTY_JSON
                SyncMutationOperation.UPSERT ->
                    objectMapper.writeValueAsString(
                        mutation.payload ?: objectMapper.createObjectNode(),
                    )
            }
        val resolution =
            SyncConflictResolver.resolve(
                SyncMutationInput(
                    entityType = mutation.entityType,
                    entityId = mutation.entityId,
                    operation = mutation.operation,
                    clientRevision = mutation.clientRevision,
                    updatedAt = mutation.updatedAt,
                    payloadJson = payloadJson,
                ),
                existing,
            )
        if (resolution.decision == SyncApplyDecision.KEEP_EXISTING) {
            return SyncMutationResultItem(
                entityType = mutation.entityType,
                entityId = mutation.entityId,
                status = "conflict",
                serverRevision = existingEntity?.serverRevision,
                conflictCode = resolution.conflictCode,
            )
        }
        val serverRevision = allocateRevision(ownerKey)
        val deletedAt =
            if (mutation.operation == SyncMutationOperation.DELETE) {
                mutation.updatedAt
            } else {
                null
            }
        val saved =
            if (existingEntity != null) {
                existingEntity.serverRevision = serverRevision
                existingEntity.clientRevision = mutation.clientRevision
                existingEntity.updatedAt = mutation.updatedAt
                existingEntity.deletedAt = deletedAt
                existingEntity.payloadJson = payloadJson
                syncEntityRecordRepository.save(existingEntity)
            } else {
                val row =
                    SyncEntityRecordEntity(
                        id = UUID.randomUUID(),
                        ownerScopeKey = ownerKey,
                        guestInstallationId =
                            when (subject) {
                                is MahinAuthSubject.GuestInstallation -> subject.guestInstallationId
                                is MahinAuthSubject.RegisteredUser -> null
                                is MahinAuthSubject.CmsStaff -> null
                            },
                        ownerUserId =
                            when (subject) {
                                is MahinAuthSubject.RegisteredUser -> subject.userId
                                is MahinAuthSubject.GuestInstallation -> null
                                is MahinAuthSubject.CmsStaff -> null
                            },
                        entityType = mutation.entityType,
                        entityId = mutation.entityId,
                        serverRevision = serverRevision,
                        clientRevision = mutation.clientRevision,
                        updatedAt = mutation.updatedAt,
                        deletedAt = deletedAt,
                        payloadJson = payloadJson,
                    )
                syncEntityRecordRepository.save(row)
            }
        return SyncMutationResultItem(
            entityType = mutation.entityType,
            entityId = mutation.entityId,
            status = "applied",
            serverRevision = saved.serverRevision,
        )
    }

    private fun findExisting(
        subject: MahinAuthSubject,
        entityType: String,
        entityId: UUID,
    ): SyncEntityRecordEntity? =
        when (subject) {
            is MahinAuthSubject.RegisteredUser ->
                syncEntityRecordRepository.findByOwnerUserIdAndEntityTypeAndEntityId(
                    subject.userId,
                    entityType,
                    entityId,
                )
            is MahinAuthSubject.GuestInstallation ->
                syncEntityRecordRepository.findByGuestInstallationIdAndEntityTypeAndEntityId(
                    subject.guestInstallationId,
                    entityType,
                    entityId,
                )
            is MahinAuthSubject.CmsStaff -> null
        }

    private fun allocateRevision(ownerKey: String): Long {
        val state =
            syncOwnerStateRepository.findForUpdate(ownerKey)
                ?: SyncOwnerStateEntity(ownerKey = ownerKey, nextServerRevision = 1)
        val revision = state.nextServerRevision
        state.nextServerRevision = revision + 1
        syncOwnerStateRepository.save(state)
        return revision
    }

    private fun currentRevision(ownerKey: String): Long {
        val state = syncOwnerStateRepository.findById(ownerKey).orElse(null)
        return (state?.nextServerRevision ?: 1L) - 1L
    }

    @Suppress("ReturnCount")
    private fun loadIdempotent(
        ownerKey: String,
        mutation: SyncMutationItem,
    ): SyncMutationResultItem? {
        val key = SyncIdempotencyEntity.Key(ownerKey, mutation.idempotencyKey)
        val stored = syncIdempotencyRepository.findById(key).orElse(null) ?: return null
        val fingerprint = fingerprint(mutation)
        if (stored.mutationFingerprint != fingerprint) {
            return SyncMutationResultItem(
                entityType = mutation.entityType,
                entityId = mutation.entityId,
                status = "conflict",
                conflictCode = "idempotency_key_reuse",
            )
        }
        return objectMapper.readValue(stored.responseJson, SyncMutationResultItem::class.java)
    }

    private fun storeIdempotent(
        ownerKey: String,
        mutation: SyncMutationItem,
        result: SyncMutationResultItem,
    ) {
        val entity =
            SyncIdempotencyEntity(
                ownerKey = ownerKey,
                idempotencyKey = mutation.idempotencyKey,
                mutationFingerprint = fingerprint(mutation),
                responseJson = objectMapper.writeValueAsString(result),
                createdAt = Instant.now(),
            )
        syncIdempotencyRepository.save(entity)
    }

    private fun fingerprint(mutation: SyncMutationItem): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val payload = mutation.payload?.toString() ?: ""
        val raw =
            listOf(
                mutation.entityType,
                mutation.entityId.toString(),
                mutation.operation.name,
                mutation.updatedAt.toString(),
                payload,
            ).joinToString("|")
        return digest.digest(raw.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }

    private fun parsePayload(
        payloadJson: String,
        deletedAt: Instant?,
    ): JsonNode? {
        if (deletedAt != null) {
            return null
        }
        return objectMapper.readTree(payloadJson)
    }

    companion object {
        private const val MAX_PULL_LIMIT = 200
        private const val EMPTY_JSON = "{}"
    }
}
