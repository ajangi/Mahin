package dev.mahin.core.sync

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoOpSyncOutbox
    @Inject
    constructor() : SyncOutbox {
        override suspend fun enqueue(mutation: SyncMutation) = Unit

        override suspend fun pending(): List<SyncMutation> = emptyList()
    }
