package dev.mahin.backend.identity

import dev.mahin.backend.device.persistence.DeviceInstallationRepository
import dev.mahin.backend.identity.persistence.GuestInstallationEntity
import dev.mahin.backend.identity.persistence.GuestInstallationRepository
import dev.mahin.backend.sync.ExistingSyncEntity
import dev.mahin.backend.sync.SyncApplyDecision
import dev.mahin.backend.sync.SyncConflictResolver
import dev.mahin.backend.sync.SyncMutationInput
import dev.mahin.backend.sync.SyncMutationOperation
import dev.mahin.backend.sync.persistence.SyncEntityRecordEntity
import dev.mahin.backend.sync.persistence.SyncEntityRecordRepository
import dev.mahin.backend.sync.persistence.SyncOwnerStateEntity
import dev.mahin.backend.sync.persistence.SyncOwnerStateRepository
import java.time.Instant
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class GuestConversionService(
    private val guestInstallationRepository: GuestInstallationRepository,
    private val syncEntityRecordRepository: SyncEntityRecordRepository,
    private val syncOwnerStateRepository: SyncOwnerStateRepository,
    private val deviceInstallationRepository: DeviceInstallationRepository,
) {
    @Transactional
    fun convertGuestToUser(
        localUserId: UUID,
        userId: UUID,
    ): ConvertGuestResponse {
        val guest =
            guestInstallationRepository.findByLocalUserId(localUserId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "guest_not_found")
        if (guest.linkedUserId != null && guest.linkedUserId != userId) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "guest_already_linked")
        }
        if (guest.linkedUserId == null) {
            guest.linkedUserId = userId
            guest.convertedAt = Instant.now()
            guestInstallationRepository.save(guest)
        }
        val migrated = migrateSyncEntities(guest, userId)
        migrateDevices(guest.id, userId)
        mergeOwnerState(guest.id, userId)
        return ConvertGuestResponse(
            userId = userId,
            guestInstallationId = guest.id,
            migratedEntityCount = migrated,
        )
    }

    private fun migrateSyncEntities(
        guest: GuestInstallationEntity,
        userId: UUID,
    ): Int {
        val userKey = "user:$userId"
        val records = syncEntityRecordRepository.findGuestChangesAfter(guest.id, 0)
        records.forEach { guestRecord ->
            val existing =
                syncEntityRecordRepository.findByOwnerUserIdAndEntityTypeAndEntityId(
                    userId,
                    guestRecord.entityType,
                    guestRecord.entityId,
                )
            if (existing == null) {
                assignRecordToUser(guestRecord, userId, userKey)
                syncEntityRecordRepository.save(guestRecord)
            } else {
                mergeCollision(guestRecord, existing, userId, userKey)
            }
        }
        return records.size
    }

    private fun mergeCollision(
        guestRecord: SyncEntityRecordEntity,
        userRecord: SyncEntityRecordEntity,
        userId: UUID,
        userKey: String,
    ) {
        val incoming = guestRecord.toMutationInput()
        val resolution =
            SyncConflictResolver.resolve(
                incoming,
                userRecord.toExistingEntity(),
            )
        if (resolution.decision == SyncApplyDecision.APPLY) {
            applyGuestWin(guestRecord, userRecord, userId, userKey)
            syncEntityRecordRepository.delete(guestRecord)
        } else {
            syncEntityRecordRepository.delete(guestRecord)
        }
    }

    private fun applyGuestWin(
        guestRecord: SyncEntityRecordEntity,
        userRecord: SyncEntityRecordEntity,
        userId: UUID,
        userKey: String,
    ) {
        userRecord.serverRevision = allocateRevision(userKey)
        userRecord.clientRevision = guestRecord.clientRevision
        userRecord.updatedAt = guestRecord.updatedAt
        userRecord.deletedAt = guestRecord.deletedAt
        userRecord.payloadJson = guestRecord.payloadJson
        userRecord.ownerUserId = userId
        userRecord.guestInstallationId = null
        userRecord.ownerScopeKey = userKey
        syncEntityRecordRepository.save(userRecord)
    }

    private fun assignRecordToUser(
        record: SyncEntityRecordEntity,
        userId: UUID,
        userKey: String,
    ) {
        record.ownerUserId = userId
        record.guestInstallationId = null
        record.ownerScopeKey = userKey
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

    private fun SyncEntityRecordEntity.toMutationInput(): SyncMutationInput {
        val operation =
            if (deletedAt != null) {
                SyncMutationOperation.DELETE
            } else {
                SyncMutationOperation.UPSERT
            }
        val mutationUpdatedAt = deletedAt ?: updatedAt
        return SyncMutationInput(
            entityType = entityType,
            entityId = entityId,
            operation = operation,
            clientRevision = clientRevision,
            updatedAt = mutationUpdatedAt,
            payloadJson = payloadJson,
        )
    }

    private fun SyncEntityRecordEntity.toExistingEntity(): ExistingSyncEntity =
        ExistingSyncEntity(
            serverRevision = serverRevision,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            payloadJson = payloadJson,
        )

    private fun migrateDevices(
        guestInstallationId: UUID,
        userId: UUID,
    ) {
        deviceInstallationRepository.findAllByGuestInstallationId(guestInstallationId).forEach { device ->
            device.ownerUserId = userId
            device.guestInstallationId = null
            deviceInstallationRepository.save(device)
        }
    }

    private fun mergeOwnerState(
        guestInstallationId: UUID,
        userId: UUID,
    ) {
        val guestKey = "guest:$guestInstallationId"
        val userKey = "user:$userId"
        val guestState = syncOwnerStateRepository.findById(guestKey).orElse(null)
        val userState =
            syncOwnerStateRepository.findById(userKey).orElse(
                SyncOwnerStateEntity(ownerKey = userKey, nextServerRevision = 1),
            )
        if (guestState != null) {
            userState.nextServerRevision = maxOf(userState.nextServerRevision, guestState.nextServerRevision)
            syncOwnerStateRepository.save(userState)
            syncOwnerStateRepository.delete(guestState)
        } else if (!syncOwnerStateRepository.existsById(userKey)) {
            syncOwnerStateRepository.save(userState)
        }
    }
}
