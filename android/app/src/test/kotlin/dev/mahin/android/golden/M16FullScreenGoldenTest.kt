package dev.mahin.android.golden

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureScreenRoboImage
import dev.mahin.android.cycle.editor.DayEditorManageCategoriesPanel
import dev.mahin.android.cycle.editor.DayEditorSexualOptInDialog
import dev.mahin.android.cycle.editor.DayEditorSheetContent
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.designsystem.ProvideReducedMotion
import dev.mahin.core.testing.ViewModelStoreClearingRule
import dev.mahin.core.testing.roborazzi.MahinRoborazzi
import dev.mahin.core.testing.roborazzi.captureMahinFullScreenGolden
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "fa-rIR-w411dp-h891dp-xxhdpi")
class M16FullScreenGoldenTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    @Test fun editor_cycle_light_scale10() = captureEditor(M16GoldenFixtures.cycleEditor(), false, 1f)

    @Test fun editor_cycle_light_scale13() = captureEditor(M16GoldenFixtures.cycleEditor(), false, 1.3f)

    @Test fun editor_cycle_dark_scale10() = captureEditor(M16GoldenFixtures.cycleEditor(), true, 1f)

    @Test fun editor_cycle_dark_scale13() = captureEditor(M16GoldenFixtures.cycleEditor(), true, 1.3f)

    @Test fun editor_ttc_light_scale10() = captureEditor(M16GoldenFixtures.ttcEditor(), false, 1f)

    @Test fun editor_ttc_light_scale13() = captureEditor(M16GoldenFixtures.ttcEditor(), false, 1.3f)

    @Test fun editor_ttc_dark_scale10() = captureEditor(M16GoldenFixtures.ttcEditor(), true, 1f)

    @Test fun editor_ttc_dark_scale13() = captureEditor(M16GoldenFixtures.ttcEditor(), true, 1.3f)

    @Test fun editor_pregnancy_light_scale10() = captureEditor(M16GoldenFixtures.pregnancyEditor(), false, 1f)

    @Test fun editor_pregnancy_light_scale13() = captureEditor(M16GoldenFixtures.pregnancyEditor(), false, 1.3f)

    @Test fun editor_pregnancy_dark_scale10() = captureEditor(M16GoldenFixtures.pregnancyEditor(), true, 1f)

    @Test fun editor_pregnancy_dark_scale13() = captureEditor(M16GoldenFixtures.pregnancyEditor(), true, 1.3f)

    @Test fun editor_sexualOptIn_light_scale10() = captureOptIn(false, 1f)

    @Test fun editor_sexualOptIn_light_scale13() = captureOptIn(false, 1.3f)

    @Test fun editor_sexualOptIn_dark_scale10() = captureOptIn(true, 1f)

    @Test fun editor_sexualOptIn_dark_scale13() = captureOptIn(true, 1.3f)

    @Test fun editor_manageCategories_light_scale10() = captureManage(false, 1f)

    @Test fun editor_manageCategories_light_scale13() = captureManage(false, 1.3f)

    @Test fun editor_manageCategories_dark_scale10() = captureManage(true, 1f)

    @Test fun editor_manageCategories_dark_scale13() = captureManage(true, 1.3f)

    private fun captureEditor(
        state: dev.mahin.android.cycle.editor.DayEditorUiState,
        dark: Boolean,
        scale: Float,
    ) {
        androidComposeRule.captureMahinFullScreenGolden(dark, scale) {
            DayEditorSheetContent(
                date = state.date ?: LocalDate.of(2025, 3, 15),
                state = state,
                onTogglePeriod = {},
                onToggleSymptom = {},
                onToggleMood = {},
                onSetPain = { _, _ -> },
                onOpenManage = {},
                onRequestSexualOptIn = {},
            )
        }
    }

    @OptIn(ExperimentalRoborazziApi::class)
    private fun captureOptIn(
        dark: Boolean,
        scale: Float,
    ) {
        if (dark) {
            RuntimeEnvironment.setQualifiers("+night")
        } else {
            RuntimeEnvironment.setQualifiers("fa-rIR-w411dp-h891dp-xxhdpi")
        }
        RuntimeEnvironment.setFontScale(scale)
        androidComposeRule.setContent {
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl,
                LocalDensity provides
                    Density(
                        density = baseDensity.density,
                        fontScale = scale,
                    ),
            ) {
                MahinTheme(darkTheme = dark) {
                    ProvideReducedMotion(reducedMotion = true) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background,
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                DayEditorSexualOptInDialog(onConfirm = {}, onDismiss = {})
                            }
                        }
                    }
                }
            }
        }
        androidComposeRule.waitForIdle()
        captureScreenRoboImage(roborazziOptions = MahinRoborazzi.options)
    }

    private fun captureManage(
        dark: Boolean,
        scale: Float,
    ) {
        androidComposeRule.captureMahinFullScreenGolden(dark, scale) {
            DayEditorManageCategoriesPanel(
                state = M16GoldenFixtures.manageCategories(),
                onLifestyleToggle = { _, _ -> },
                onRequestSexualOptIn = {},
            )
        }
    }
}
