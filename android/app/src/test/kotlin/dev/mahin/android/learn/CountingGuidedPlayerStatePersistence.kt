package dev.mahin.android.learn

import dev.mahin.core.content.WellnessContentLocalStore
import dev.mahin.core.database.entity.GuidedPlayerStateEntity

class CountingGuidedPlayerStatePersistence(
    private val delegate: WellnessContentLocalStore,
) : GuidedPlayerStatePersistence {
    var saveCount: Int = 0

    override suspend fun savePlayerState(
        programId: String,
        sessionId: String,
        stepIndex: Int,
        stepElapsedMs: Long,
        paused: Boolean,
    ) {
        saveCount += 1
        delegate.savePlayerState(programId, sessionId, stepIndex, stepElapsedMs, paused)
    }

    override suspend fun loadPlayerState(): GuidedPlayerStateEntity? = delegate.loadPlayerState()

    override suspend fun clearPlayerState() {
        delegate.clearPlayerState()
    }
}
