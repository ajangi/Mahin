package dev.mahin.backend.sync

import java.time.Instant
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SyncConflictResolverTest {
    private val entityId = UUID.randomUUID()
    private val base = Instant.parse("2026-01-01T00:00:00Z")

    @Test
    fun appliesWhenNoExistingRow() {
        val result =
            SyncConflictResolver.resolve(
                mutation(operation = SyncMutationOperation.UPSERT, updatedAt = base),
                existing = null,
            )
        assertThat(result.decision).isEqualTo(SyncApplyDecision.APPLY)
    }

    @Test
    fun tombstoneWinsOverStaleUpsert() {
        val result =
            SyncConflictResolver.resolve(
                mutation(operation = SyncMutationOperation.UPSERT, updatedAt = base),
                existing =
                    ExistingSyncEntity(
                        serverRevision = 2,
                        updatedAt = base.plusSeconds(10),
                        deletedAt = base.plusSeconds(5),
                        payloadJson = "{}",
                    ),
            )
        assertThat(result.decision).isEqualTo(SyncApplyDecision.KEEP_EXISTING)
        assertThat(result.conflictCode).isEqualTo("stale_after_tombstone")
    }

    @Test
    fun deleteAppliesWhenNewerThanExisting() {
        val result =
            SyncConflictResolver.resolve(
                mutation(operation = SyncMutationOperation.DELETE, updatedAt = base.plusSeconds(20)),
                existing =
                    ExistingSyncEntity(
                        serverRevision = 1,
                        updatedAt = base,
                        deletedAt = null,
                        payloadJson = """{"v":1}""",
                    ),
            )
        assertThat(result.decision).isEqualTo(SyncApplyDecision.APPLY)
    }

    @Test
    fun staleDeleteKeepsNewerExistingRow() {
        val result =
            SyncConflictResolver.resolve(
                mutation(operation = SyncMutationOperation.DELETE, updatedAt = base),
                existing =
                    ExistingSyncEntity(
                        serverRevision = 2,
                        updatedAt = base.plusSeconds(10),
                        deletedAt = null,
                        payloadJson = """{"v":2}""",
                    ),
            )
        assertThat(result.decision).isEqualTo(SyncApplyDecision.KEEP_EXISTING)
        assertThat(result.conflictCode).isEqualTo("updated_at_stale")
    }

    @Test
    fun lastWriteWinsByUpdatedAt() {
        val result =
            SyncConflictResolver.resolve(
                mutation(operation = SyncMutationOperation.UPSERT, updatedAt = base.plusSeconds(1)),
                existing =
                    ExistingSyncEntity(
                        serverRevision = 1,
                        updatedAt = base,
                        deletedAt = null,
                        payloadJson = "{}",
                    ),
            )
        assertThat(result.decision).isEqualTo(SyncApplyDecision.APPLY)
    }

    private fun mutation(
        operation: SyncMutationOperation,
        updatedAt: Instant,
    ): SyncMutationInput =
        SyncMutationInput(
            entityType = "period_record",
            entityId = entityId,
            operation = operation,
            clientRevision = 1,
            updatedAt = updatedAt,
            payloadJson = """{"flow":"light"}""",
        )
}
