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

    val hasSteps: Boolean get() = steps.isNotEmpty()

    fun snapshot(): GuidedPlayerSnapshot =
        GuidedPlayerSnapshot(
            stepIndex = stepIndex.coerceAtMost(steps.lastIndex.coerceAtLeast(0)),
            stepElapsedMs = stepElapsedMs,
            paused = paused,
            completed = completed || steps.isEmpty(),
        )

    fun restore(snapshot: GuidedPlayerSnapshot) {
        if (steps.isEmpty()) {
            stepIndex = 0
            stepElapsedMs = 0L
            paused = snapshot.paused
            completed = true
            lastTickInstant = null
            pauseStartedAt = null
            return
        }
        val maxIndex = steps.lastIndex
        stepIndex = snapshot.stepIndex.coerceIn(0, maxIndex)
        stepElapsedMs = snapshot.stepElapsedMs.coerceAtLeast(0L)
        paused = snapshot.paused
        completed = snapshot.completed
        lastTickInstant = null
        pauseStartedAt = null
    }

    fun pause() {
        if (completed || paused || steps.isEmpty()) return
        paused = true
        pauseStartedAt = clock.now()
    }

    fun resume() {
        if (completed || !paused || steps.isEmpty()) return
        pauseStartedAt = null
        paused = false
        lastTickInstant = clock.now()
    }

    fun tick(): GuidedPlayerSnapshot {
        if (completed || steps.isEmpty()) {
            completed = steps.isEmpty() || completed
            return snapshot()
        }
        val now = clock.now()
        if (!paused) {
            val previous = lastTickInstant
            if (previous != null) {
                val delta = (now.toEpochMilli() - previous.toEpochMilli()).coerceAtLeast(0L)
                applyElapsed(delta)
            }
        }
        lastTickInstant = now
        return snapshot()
    }

    fun markCompleted() {
        completed = true
        paused = false
    }

    private fun applyElapsed(deltaMs: Long) {
        var remaining = deltaMs
        while (remaining > 0 && !completed) {
            val target = stepDurationMs(steps[stepIndex])
            if (target == null) {
                stepElapsedMs += remaining
                return
            }
            val needed = target - stepElapsedMs
            if (remaining < needed) {
                stepElapsedMs += remaining
                return
            }
            remaining -= needed
            if (stepIndex >= steps.lastIndex) {
                completed = true
                stepElapsedMs = remaining
                return
            }
            stepIndex += 1
            stepElapsedMs = 0L
        }
    }

    private fun stepDurationMs(step: GuidedPlayerStep): Long? {
        val seconds = step.durationSeconds
        return if (seconds != null && seconds > 0) seconds * 1000L else null
    }
}
