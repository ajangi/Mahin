package dev.mahin.android.pregnancy

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PregnancyHubTimerMathTest {
    @Test
    fun elapsedSeconds_handlesNullAndNegativeClamp() {
        assertThat(PregnancyHubTimerMath.elapsedSeconds(10_000L, null)).isEqualTo(0L)
        assertThat(PregnancyHubTimerMath.elapsedSeconds(5_000L, 10_000L)).isEqualTo(0L)
    }

    @Test
    fun elapsedSeconds_computesWholeSeconds() {
        assertThat(PregnancyHubTimerMath.elapsedSeconds(12_500L, 10_000L)).isEqualTo(2L)
        assertThat(PregnancyHubTimerMath.elapsedSeconds(20_000L, 10_000L)).isEqualTo(10L)
    }
}
