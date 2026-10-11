package dev.mahin.android.cycle.editor

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.testing.ViewModelStoreClearingRule
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
        var openedEditor = false
        androidComposeRule.setContent {
            MahinTheme {
                DayEditorHost(
                    session = DayEditorSession(LocalDate.of(2025, 6, 15)),
                    onDismiss = {},
                )
            }
        }
        androidComposeRule.onNodeWithTag("day_editor_sheet").assertExists()
        openedEditor = true
        assertThat(openedEditor).isTrue()
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
