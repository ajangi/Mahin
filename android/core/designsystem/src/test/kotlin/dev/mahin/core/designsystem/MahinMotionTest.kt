package dev.mahin.core.designsystem

import android.provider.Settings
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.common.truth.Truth.assertThat
import java.io.File
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
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
        val motion = designTokensJson().getJSONObject("motionMs")
        assertThat(MahinMotionDuration.FAST_MS).isEqualTo(motion.getInt("fast"))
        assertThat(MahinMotionDuration.NORMAL_MS).isEqualTo(motion.getInt("normal"))
        assertThat(MahinMotionDuration.SLOW_MS).isEqualTo(motion.getInt("slow"))
        val easing = designTokensJson().getJSONObject("motionEasing")
        assertThat(MahinMotionEasing.STANDARD).isEqualTo(easing.getString("standard"))
        assertThat(MahinMotionEasing.EMPHASIZED).isEqualTo(easing.getString("emphasized"))
        assertThat(MahinMotionEasing.EMPHASIZED).isNotEqualTo(MahinMotionEasing.STANDARD)
        assertThat(MahinMotionEasingCurves.standard).isNotEqualTo(MahinMotionEasingCurves.emphasized)
    }

    @Test
    fun mahinTheme_readsAnimatorDurationScaleZero_asReducedMotion() {
        val resolver = RuntimeEnvironment.getApplication().contentResolver
        Settings.Global.putFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 0f)
        var reduced = false
        composeRule.setContent {
            MahinTheme {
                reduced = LocalReducedMotion.current
                Text("test")
            }
        }
        assertThat(reduced).isTrue()
    }

    @Test
    fun mahinTheme_readsAnimatorDurationScaleOne_asNormalMotion() {
        val resolver = RuntimeEnvironment.getApplication().contentResolver
        Settings.Global.putFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        var reduced = true
        composeRule.setContent {
            MahinTheme {
                reduced = LocalReducedMotion.current
                Text("test")
            }
        }
        assertThat(reduced).isFalse()
    }

    private fun designTokensJson(): JSONObject {
        val tokensFile = locateDesignTokensFile()
        return JSONObject(tokensFile.readText())
    }

    private fun locateDesignTokensFile(): File {
        var dir = File(System.getProperty("user.dir"))
        while (true) {
            val candidate = File(dir, "design/tokens.json")
            if (candidate.isFile) return candidate
            val parent = dir.parentFile ?: break
            dir = parent
        }
        error("design/tokens.json not found from ${System.getProperty("user.dir")}")
    }
}
