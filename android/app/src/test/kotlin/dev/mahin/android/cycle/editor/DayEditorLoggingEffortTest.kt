package dev.mahin.android.cycle.editor

import androidx.activity.ComponentActivity
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.LogEditorCategory
import dev.mahin.core.testing.ViewModelStoreClearingRule
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR", manifest = Config.NONE)
class DayEditorLoggingEffortTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    @Test
    fun quickLog_opensDayEditorSheet() {
        val date = LocalDate.of(2025, 6, 15)
        androidComposeRule.setContent {
            MahinTheme {
                DayEditorSheetContent(
                    date = date,
                    state =
                        DayEditorUiState(
                            orderedCategories = listOf(LogEditorCategory.PERIOD_FLOW),
                        ),
                    onTogglePeriod = {},
                    onToggleSymptom = {},
                    onToggleMood = {},
                    onSetPain = { _, _ -> },
                    onOpenManage = {},
                    onRequestSexualOptIn = {},
                    modifier = Modifier.testTag("day_editor_sheet"),
                )
            }
        }
        androidComposeRule.onNodeWithTag("day_editor_sheet").assertExists()
        androidComposeRule.onNodeWithTag("day_editor_period_started").assertExists()
    }

    @Test
    fun periodSymptomMood_eachOneTapWithoutSave() {
        val date = LocalDate.of(2025, 6, 15)
        androidComposeRule.setContent {
            MahinTheme {
                DayEditorSheetContent(
                    date = date,
                    state =
                        DayEditorUiState(
                            orderedCategories =
                                listOf(
                                    dev.mahin.core.model.LogEditorCategory.PERIOD_FLOW,
                                    dev.mahin.core.model.LogEditorCategory.SYMPTOMS,
                                    dev.mahin.core.model.LogEditorCategory.MOOD,
                                ),
                        ),
                    onTogglePeriod = {},
                    onToggleSymptom = {},
                    onToggleMood = {},
                    onSetPain = { _, _ -> },
                    onOpenManage = {},
                    onRequestSexualOptIn = {},
                )
            }
        }
        androidComposeRule.onNodeWithTag("day_editor_period_started").performClick()
        androidComposeRule.onNodeWithTag("day_editor_symptom_خستگی").performClick()
        androidComposeRule.onNodeWithTag("day_editor_mood_آرام").performClick()
    }
}
