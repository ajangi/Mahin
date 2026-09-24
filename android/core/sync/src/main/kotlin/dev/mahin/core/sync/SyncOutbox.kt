package dev.mahin.core.sync

import java.util.UUID

data class SyncMutation(
    val id: UUID,
    val entityType: String,
    val entityId: UUID,
    val idempotencyKey: String,
    val createdAtEpochMs: Long,
)

interface SyncOutbox {
    suspend fun enqueue(mutation: SyncMutation)
    suspend fun pending(): List<SyncMutation>
}

class InMemorySyncOutbox : SyncOutbox {
    private val items = mutableListOf<SyncMutation>()
    override suspend fun enqueue(mutation: SyncMutation) {
        items += mutation
    }

    override suspend fun pending(): List<SyncMutation> = items.toList()
}
