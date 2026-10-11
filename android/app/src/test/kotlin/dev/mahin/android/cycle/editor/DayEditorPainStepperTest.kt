package dev.mahin.android.cycle.editor

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.LogEditorCategory
import dev.mahin.core.model.PainLocation
import dev.mahin.core.testing.ViewModelStoreClearingRule
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR", manifest = Config.NONE)
class DayEditorPainStepperTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    @Test
    fun painStepper_increasesSeverity() {
        var severity = 3
        androidComposeRule.setContent {
            MahinTheme {
                DayEditorSheetContent(
                    date = LocalDate.of(2025, 6, 1),
                    state =
                        DayEditorUiState(
                            orderedCategories = listOf(LogEditorCategory.PAIN),
                            painLocation = PainLocation.HEAD,
                            painSeverity = severity,
                        ),
                    onSetPain = { _, level -> severity = level },
                    onAdjustPainSeverity = { severity = it },
                )
            }
        }
        androidComposeRule.onNodeWithTag("day_editor_pain_increase").performClick()
        assertThat(severity).isEqualTo(4)
    }

    @Test
    fun painStepper_showsPersianZero() {
        androidComposeRule.setContent {
            MahinTheme {
                DayEditorSheetContent(
                    date = LocalDate.of(2025, 6, 1),
                    state =
                        DayEditorUiState(
                            orderedCategories = listOf(LogEditorCategory.PAIN),
                            painLocation = PainLocation.HEAD,
                            painSeverity = 0,
                        ),
                )
            }
        }
        androidComposeRule.onNodeWithTag("day_editor_pain_severity").assertTextContains("۰", substring = true)
    }

    @Test
    fun customSymptomSection_exposesAddControl() {
        androidComposeRule.setContent {
            MahinTheme {
                DayEditorSheetContent(
                    date = LocalDate.of(2025, 6, 1),
                    state =
                        DayEditorUiState(
                            orderedCategories = listOf(LogEditorCategory.SYMPTOMS),
                        ),
                )
            }
        }
        androidComposeRule.onNodeWithTag("day_editor_custom_symptom_add").assertExists()
    }

    @Test
    fun painStepper_showsPersianTen() {
        androidComposeRule.setContent {
            MahinTheme {
                DayEditorSheetContent(
                    date = LocalDate.of(2025, 6, 1),
                    state =
                        DayEditorUiState(
                            orderedCategories = listOf(LogEditorCategory.PAIN),
                            painLocation = PainLocation.HEAD,
                            painSeverity = 10,
                        ),
                )
            }
        }
        androidComposeRule.onNodeWithTag("day_editor_pain_severity").assertTextContains("۱۰", substring = true)
    }
}
