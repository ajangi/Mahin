package dev.mahin.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MahinMotionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun localReducedMotion_collapsesDurations() {
        var duration = MahinMotionDuration.SLOW_MS
        composeRule.setContent {
            ProvideReducedMotion(reducedMotion = true) {
                duration = mahinMotionDurationMs(MahinMotionDuration.SLOW_MS, respectSystemSetting = false)
                Text("test")
            }
        }
        assertThat(duration).isEqualTo(MahinMotionDuration.INSTANT_MS)
    }

    @Test
    fun motionTokenConstants_matchDesignTokensJson() {
        assertThat(MahinMotionDuration.FAST_MS).isEqualTo(150)
        assertThat(MahinMotionDuration.NORMAL_MS).isEqualTo(250)
        assertThat(MahinMotionEasing.STANDARD).contains("cubic-bezier")
    }
}
