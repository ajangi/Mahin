package dev.mahin.backend.sync

import java.time.Instant

enum class SyncMutationOperation {
    UPSERT,
    DELETE,
}

data class SyncMutationInput(
    val entityType: String,
    val entityId: java.util.UUID,
    val operation: SyncMutationOperation,
    val clientRevision: Long?,
    val updatedAt: Instant,
    val payloadJson: String,
)

data class ExistingSyncEntity(
    val serverRevision: Long,
    val updatedAt: Instant,
    val deletedAt: Instant?,
    val payloadJson: String,
)

enum class SyncApplyDecision {
    APPLY,
    KEEP_EXISTING,
}

data class SyncApplyResult(
    val decision: SyncApplyDecision,
    val conflictCode: String? = null,
)

@Suppress("ReturnCount")
object SyncConflictResolver {
    fun resolve(
        incoming: SyncMutationInput,
        existing: ExistingSyncEntity?,
    ): SyncApplyResult {
        if (existing == null) {
            return SyncApplyResult(SyncApplyDecision.APPLY)
        }
        if (existing.deletedAt != null) {
            if (incoming.operation == SyncMutationOperation.DELETE) {
                return SyncApplyResult(SyncApplyDecision.KEEP_EXISTING, "already_deleted")
            }
            if (incoming.updatedAt.isBefore(existing.deletedAt)) {
                return SyncApplyResult(SyncApplyDecision.KEEP_EXISTING, "stale_after_tombstone")
            }
        }
        if (incoming.operation == SyncMutationOperation.DELETE) {
            if (incoming.updatedAt.isBefore(existing.updatedAt)) {
                return SyncApplyResult(SyncApplyDecision.KEEP_EXISTING, "updated_at_stale")
            }
            return SyncApplyResult(SyncApplyDecision.APPLY)
        }
        if (incoming.updatedAt.isAfter(existing.updatedAt)) {
            return SyncApplyResult(SyncApplyDecision.APPLY)
        }
        if (incoming.updatedAt == existing.updatedAt) {
            val incomingRevision = incoming.clientRevision ?: 0L
            val existingRevision = existing.serverRevision
            if (incomingRevision >= existingRevision) {
                return SyncApplyResult(SyncApplyDecision.APPLY)
            }
            return SyncApplyResult(SyncApplyDecision.KEEP_EXISTING, "revision_stale")
        }
        return SyncApplyResult(SyncApplyDecision.KEEP_EXISTING, "updated_at_stale")
    }
}
