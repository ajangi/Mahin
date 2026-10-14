package dev.mahin.android.learn

import dev.mahin.core.content.WellnessContentLocalStore
import dev.mahin.core.database.entity.GuidedPlayerStateEntity

internal interface GuidedPlayerStatePersistence {
    suspend fun savePlayerState(
        programId: String,
        sessionId: String,
        stepIndex: Int,
        stepElapsedMs: Long,
        paused: Boolean,
    )

    suspend fun loadPlayerState(): GuidedPlayerStateEntity?

    suspend fun clearPlayerState()
}

internal fun WellnessContentLocalStore.asGuidedPlayerStatePersistence(): GuidedPlayerStatePersistence =
    object : GuidedPlayerStatePersistence {
        override suspend fun savePlayerState(
            programId: String,
            sessionId: String,
            stepIndex: Int,
            stepElapsedMs: Long,
            paused: Boolean,
        ) {
            this@asGuidedPlayerStatePersistence.savePlayerState(
                programId,
                sessionId,
                stepIndex,
                stepElapsedMs,
                paused,
            )
        }

        override suspend fun loadPlayerState(): GuidedPlayerStateEntity? =
            this@asGuidedPlayerStatePersistence.loadPlayerState()

        override suspend fun clearPlayerState() {
            this@asGuidedPlayerStatePersistence.clearPlayerState()
        }
    }
