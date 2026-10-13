package dev.mahin.domain.content

import dev.mahin.core.common.InstantClock
import java.time.Instant

data class GuidedPlayerStep(
    val instruction: String,
    val durationSeconds: Int?,
    val reps: Int?,
    val restSeconds: Int?,
)

data class GuidedPlayerSnapshot(
    val stepIndex: Int,
    val stepElapsedMs: Long,
    val paused: Boolean,
    val completed: Boolean,
)

/**
 * Deterministic step timer for guided wellness sessions. Time advances only via [tick] using [clock].
 */
class GuidedSessionPlayer(
    private val steps: List<GuidedPlayerStep>,
    private val clock: InstantClock,
) {
    private var stepIndex: Int = 0
    private var stepElapsedMs: Long = 0L
    private var paused: Boolean = false
    private var completed: Boolean = false
    private var lastTickInstant: Instant? = null
    private var pauseStartedAt: Instant? = null
    private var accumulatedPauseMs: Long = 0L

    fun snapshot(): GuidedPlayerSnapshot =
        GuidedPlayerSnapshot(
            stepIndex = stepIndex,
            stepElapsedMs = stepElapsedMs,
            paused = paused,
            completed = completed,
        )

    fun restore(snapshot: GuidedPlayerSnapshot) {
        require(snapshot.stepIndex in steps.indices || snapshot.completed) {
            "Invalid restored step index"
        }
        stepIndex = snapshot.stepIndex.coerceIn(0, steps.lastIndex.coerceAtLeast(0))
        stepElapsedMs = snapshot.stepElapsedMs.coerceAtLeast(0L)
        paused = snapshot.paused
        completed = snapshot.completed
        lastTickInstant = null
        pauseStartedAt = null
        accumulatedPauseMs = 0L
    }

    fun pause() {
        if (completed || paused) return
        paused = true
        pauseStartedAt = clock.now()
    }

    fun resume() {
        if (completed || !paused) return
        val pauseStart = pauseStartedAt
        if (pauseStart != null) {
            accumulatedPauseMs += (clock.now().toEpochMilli() - pauseStart.toEpochMilli()).coerceAtLeast(0L)
        }
        pauseStartedAt = null
        paused = false
        lastTickInstant = clock.now()
    }

    fun tick(): GuidedPlayerSnapshot {
        if (completed) return snapshot()
        val now = clock.now()
        if (!paused) {
            val previous = lastTickInstant
            if (previous != null) {
                val delta = (now.toEpochMilli() - previous.toEpochMilli()).coerceAtLeast(0L)
                stepElapsedMs += delta
                val step = steps[stepIndex]
                val targetMs = stepDurationMs(step)
                if (targetMs != null && stepElapsedMs >= targetMs) {
                    advanceStep()
                }
            }
        }
        lastTickInstant = now
        return snapshot()
    }

    fun markCompleted() {
        completed = true
        paused = false
    }

    private fun advanceStep() {
        if (stepIndex >= steps.lastIndex) {
            completed = true
            return
        }
        stepIndex += 1
        stepElapsedMs = 0L
    }

    private fun stepDurationMs(step: GuidedPlayerStep): Long? {
        val seconds = step.durationSeconds
        return if (seconds != null && seconds > 0) seconds * 1000L else null
    }
}
