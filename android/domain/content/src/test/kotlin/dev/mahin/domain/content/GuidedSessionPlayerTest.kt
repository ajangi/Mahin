package dev.mahin.domain.content

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.common.InstantClock
import java.time.Instant
import org.junit.Test

class GuidedSessionPlayerTest {
    private var nowMs: Long = 0L

    private val clock =
        InstantClock {
            Instant.ofEpochMilli(nowMs)
        }

    private val steps =
        listOf(
            GuidedPlayerStep(instruction = "انقباض", durationSeconds = 3, reps = null, restSeconds = null),
            GuidedPlayerStep(instruction = "استراحت", durationSeconds = 2, reps = null, restSeconds = null),
        )

    @Test
    fun advancesStepsDeterministicallyWithVirtualTime() {
        val player = GuidedSessionPlayer(steps, clock)
        player.tick()
        nowMs += 1_000
        assertThat(player.tick().stepIndex).isEqualTo(0)
        assertThat(player.snapshot().stepElapsedMs).isEqualTo(1_000)
        nowMs += 2_000
        val afterStepAdvance = player.tick()
        assertThat(afterStepAdvance.stepIndex).isEqualTo(1)
        assertThat(afterStepAdvance.stepElapsedMs).isEqualTo(0)
        nowMs += 2_000
        assertThat(player.tick().completed).isTrue()
    }

    @Test
    fun emptySteps_noOpWithoutCrash() {
        val player = GuidedSessionPlayer(emptyList(), clock)
        player.restore(GuidedPlayerSnapshot(stepIndex = 2, stepElapsedMs = 9_000, paused = false, completed = false))
        assertThat(player.tick().completed).isTrue()
        player.pause()
        player.resume()
        assertThat(player.snapshot().stepIndex).isEqualTo(0)
    }

    @Test
    fun longTick_carriesOverflowAcrossMultipleSteps() {
        val player = GuidedSessionPlayer(steps, clock)
        player.tick()
        nowMs += 4_500
        val snap = player.tick()
        assertThat(snap.stepIndex).isEqualTo(1)
        assertThat(snap.stepElapsedMs).isEqualTo(1_500)
        assertThat(snap.completed).isFalse()
        nowMs += 1_000
        assertThat(player.tick().completed).isTrue()
    }

    @Test
    fun pauseFreezesElapsedTimeUntilResume() {
        val player = GuidedSessionPlayer(steps, clock)
        player.tick()
        nowMs += 1_500
        player.tick()
        player.pause()
        nowMs += 5_000
        player.tick()
        assertThat(player.snapshot().stepElapsedMs).isEqualTo(1_500)
        player.resume()
        nowMs += 1_400
        val snap = player.tick()
        assertThat(snap.stepIndex).isEqualTo(0)
        assertThat(snap.stepElapsedMs).isEqualTo(2_900)
    }

    @Test
    fun restoreSurvivesProcessDeath() {
        val player = GuidedSessionPlayer(steps, clock)
        player.tick()
        nowMs += 1_000
        player.tick()
        player.pause()
        val saved = player.snapshot()
        val restored = GuidedSessionPlayer(steps, clock)
        restored.restore(saved)
        assertThat(restored.snapshot().paused).isTrue()
        assertThat(restored.snapshot().stepIndex).isEqualTo(0)
        restored.resume()
        nowMs += 2_100
        assertThat(restored.tick().stepIndex).isEqualTo(1)
    }
}
