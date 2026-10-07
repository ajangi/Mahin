package dev.mahin.android.golden

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.mahin.android.demo.IconCatalogueFamilyGoldenSheet
import dev.mahin.android.demo.IconCatalogueRtlMirrorCompare
import dev.mahin.core.testing.roborazzi.captureMahinFullScreenGolden
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * M14b icon-sheet goldens (fa-IR RTL, light/dark). Output: [android/app/src/test/screenshots].
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "fa-rIR-w411dp-h891dp-xxhdpi")
class M14bIconSheetGoldenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun icons_nav_light() = captureFamily("nav", "ناوبری", darkTheme = false)

    @Test
    fun icons_nav_dark() = captureFamily("nav", "ناوبری", darkTheme = true)

    @Test
    fun icons_action_light() = captureFamily("action", "عمل", darkTheme = false)

    @Test
    fun icons_action_dark() = captureFamily("action", "عمل", darkTheme = true)

    @Test
    fun icons_flow_light() = captureFamily("flow", "جریان خون", darkTheme = false)

    @Test
    fun icons_flow_dark() = captureFamily("flow", "جریان خون", darkTheme = true)

    @Test
    fun icons_symptom_light() = captureFamily("symptom", "علائم", darkTheme = false)

    @Test
    fun icons_symptom_dark() = captureFamily("symptom", "علائم", darkTheme = true)

    @Test
    fun icons_mood_light() = captureFamily("mood", "خلق", darkTheme = false)

    @Test
    fun icons_mood_dark() = captureFamily("mood", "خلق", darkTheme = true)

    @Test
    fun icons_discharge_light() = captureFamily("discharge", "ترشح", darkTheme = false)

    @Test
    fun icons_discharge_dark() = captureFamily("discharge", "ترشح", darkTheme = true)

    @Test
    fun icons_tests_light() = captureFamily("tests", "آزمایش‌ها", darkTheme = false)

    @Test
    fun icons_tests_dark() = captureFamily("tests", "آزمایش‌ها", darkTheme = true)

    @Test
    fun icons_lifestyle_light() = captureFamily("lifestyle", "سبک زندگی", darkTheme = false)

    @Test
    fun icons_lifestyle_dark() = captureFamily("lifestyle", "سبک زندگی", darkTheme = true)

    @Test
    fun icons_rtl_directional_mirror_light() {
        composeRule.captureMahinFullScreenGolden(darkTheme = false, fontScale = 1f) {
            IconCatalogueRtlMirrorCompare(darkTheme = false)
        }
    }

    @Test
    fun icons_rtl_directional_mirror_dark() {
        composeRule.captureMahinFullScreenGolden(darkTheme = true, fontScale = 1f) {
            IconCatalogueRtlMirrorCompare(darkTheme = true)
        }
    }

    private fun captureFamily(
        prefix: String,
        title: String,
        darkTheme: Boolean,
    ) {
        composeRule.captureMahinFullScreenGolden(darkTheme = darkTheme, fontScale = 1f) {
            IconCatalogueFamilyGoldenSheet(
                familyPrefix = prefix,
                title = title,
                darkTheme = darkTheme,
            )
        }
    }
}
