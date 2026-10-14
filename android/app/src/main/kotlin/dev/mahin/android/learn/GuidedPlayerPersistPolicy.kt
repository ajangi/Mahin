package dev.mahin.android.learn

import dev.mahin.domain.content.GuidedPlayerSnapshot

internal object GuidedPlayerPersistPolicy {
    const val ELAPSED_CHECKPOINT_MS: Long = 5_000L

    data class Fingerprint(
        val stepIndex: Int,
        val paused: Boolean,
        val completed: Boolean,
        val elapsedCheckpointBucket: Long,
    )

    fun fingerprint(snapshot: GuidedPlayerSnapshot): Fingerprint =
        Fingerprint(
            stepIndex = snapshot.stepIndex,
            paused = snapshot.paused,
            completed = snapshot.completed,
            elapsedCheckpointBucket = snapshot.stepElapsedMs / ELAPSED_CHECKPOINT_MS,
        )

    fun shouldPersistAfterTick(
        snapshot: GuidedPlayerSnapshot,
        lastFingerprint: Fingerprint?,
    ): Boolean {
        val next = fingerprint(snapshot)
        return lastFingerprint != null && next != lastFingerprint
    }
}
