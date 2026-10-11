package dev.mahin.android.cycle.editor

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
class DayEditorOptInTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    @Test
    fun sexualActivity_hiddenUntilOptIn() {
        androidComposeRule.setContent {
            MahinTheme {
                DayEditorSheetContent(
                    date = LocalDate.of(2025, 1, 1),
                    state =
                        DayEditorUiState(
                            sexualActivityEnabled = false,
                            orderedCategories =
                                listOf(
                                    LogEditorCategory.PERIOD_FLOW,
                                    LogEditorCategory.SYMPTOMS,
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
        androidComposeRule.onNodeWithTag("day_editor_sexual_section").assertDoesNotExist()
    }

    @Test
    fun lifestyleModules_hiddenByDefault() {
        androidComposeRule.setContent {
            MahinTheme {
                DayEditorSheetContent(
                    date = LocalDate.of(2025, 1, 1),
                    state =
                        DayEditorUiState(
                            lifestyleEnabled = emptyMap(),
                            orderedCategories =
                                listOf(
                                    LogEditorCategory.PERIOD_FLOW,
                                    LogEditorCategory.SLEEP,
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
        androidComposeRule.onNodeWithTag("day_editor_lifestyle_sleep").assertDoesNotExist()
    }
}
