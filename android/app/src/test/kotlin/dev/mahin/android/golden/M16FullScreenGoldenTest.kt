package dev.mahin.android.golden

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.mahin.android.R
import dev.mahin.android.cycle.editor.DayEditorManageCategoriesPanel
import dev.mahin.android.cycle.editor.DayEditorSheetContent
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.testing.ViewModelStoreClearingRule
import dev.mahin.core.testing.roborazzi.captureMahinFullScreenGolden
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
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

    private fun captureOptIn(
        dark: Boolean,
        scale: Float,
    ) {
        androidComposeRule.captureMahinFullScreenGolden(dark, scale) {
            Column(modifier = Modifier.padding(MahinSpacing.lg)) {
                Text(
                    text = stringResource(R.string.day_editor_sexual_opt_in_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.day_editor_sexual_opt_in_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
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
