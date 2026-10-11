package dev.mahin.android.cycle

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import dev.mahin.android.cycle.editor.DayEditorManageCategoriesPanel
import dev.mahin.android.cycle.editor.DayEditorSheetContent
import dev.mahin.android.cycle.editor.DayEditorUiState
import dev.mahin.android.settings.SettingsScreenCallbacks
import dev.mahin.android.settings.SettingsScreenContent
import dev.mahin.android.settings.SettingsUiState
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
class LoggingLifestyleEnableTest {
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    @Test
    fun dayEditor_sleepHiddenUntilEnabledFromManage() {
        androidComposeRule.setContent {
            var sleepEnabled by remember { mutableStateOf(false) }
            MahinTheme {
                if (!sleepEnabled) {
                    DayEditorManageCategoriesPanel(
                        state =
                            DayEditorUiState(
                                lifestyleEnabled = mapOf(LogEditorCategory.SLEEP to false),
                            ),
                        onLifestyleToggle = { category, enabled ->
                            if (category == LogEditorCategory.SLEEP) sleepEnabled = enabled
                        },
                        onRequestSexualOptIn = {},
                    )
                } else {
                    DayEditorSheetContent(
                        date = LocalDate.of(2025, 1, 1),
                        state =
                            DayEditorUiState(
                                lifestyleEnabled = mapOf(LogEditorCategory.SLEEP to true),
                                orderedCategories = listOf(LogEditorCategory.SLEEP),
                            ),
                    )
                }
            }
        }
        androidComposeRule.onNodeWithTag("day_editor_lifestyle_sleep_switch").performClick()
        androidComposeRule.waitForIdle()
        androidComposeRule.onNodeWithTag("day_editor_lifestyle_sleep_toggle").assertExists()
    }

    @Test
    fun settingsToggle_enablesSleepInEditor() {
        androidComposeRule.setContent {
            var sleepEnabled by remember { mutableStateOf(false) }
            MahinTheme {
                if (!sleepEnabled) {
                    SettingsScreenContent(
                        state = SettingsUiState(lifestyleSleepEnabled = false),
                        callbacks =
                            SettingsScreenCallbacks(
                                onNavigateUp = {},
                                onModeSelected = {},
                                onOpenNotifications = {},
                                onOpenPrivacy = {},
                                onOpenHealthConnect = {},
                                onOpenAssistant = {},
                                onOpenDataExport = {},
                                onOpenPremium = {},
                                onOpenHistory = {},
                                onResumeCycle = {},
                                onResumeTtc = {},
                                onLifestyleModuleToggle = { category, enabled ->
                                    if (category == LogEditorCategory.SLEEP) sleepEnabled = enabled
                                },
                            ),
                    )
                } else {
                    DayEditorSheetContent(
                        date = LocalDate.of(2025, 1, 1),
                        state =
                            DayEditorUiState(
                                lifestyleEnabled = mapOf(LogEditorCategory.SLEEP to true),
                                orderedCategories = listOf(LogEditorCategory.SLEEP),
                            ),
                    )
                }
            }
        }
        androidComposeRule
            .onNodeWithTag(
                "settings_screen_list",
            ).performScrollToNode(hasTestTag("settings_lifestyle_sleep"))
        androidComposeRule.onNodeWithTag("settings_lifestyle_sleep").assertExists()
        androidComposeRule.onNodeWithTag("settings_lifestyle_sleep_switch").performClick()
        androidComposeRule.waitForIdle()
        androidComposeRule.onNodeWithTag("day_editor_lifestyle_sleep_toggle").assertExists()
    }
}
