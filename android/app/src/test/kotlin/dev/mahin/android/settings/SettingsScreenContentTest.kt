package dev.mahin.android.settings

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.ReproductiveMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR")
class SettingsScreenContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settings_showsRelocatedPrivacyAndExportEntries() {
        var openedExport = false
        composeRule.setContent {
            MahinTheme {
                SettingsScreenContent(
                    state =
                        SettingsUiState(
                            healthConnectEntryVisible = true,
                            healthAssistantEntryVisible = true,
                        ),
                    callbacks =
                        SettingsScreenCallbacks(
                            onNavigateUp = {},
                            onModeSelected = {},
                            onOpenNotifications = {},
                            onOpenPrivacy = {},
                            onOpenHealthConnect = {},
                            onOpenAssistant = {},
                            onOpenDataExport = { openedExport = true },
                            onOpenHistory = {},
                            onResumeCycle = {},
                            onResumeTtc = {},
                        ),
                )
            }
        }
        composeRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasText("خروجی داده"))
        composeRule.onNodeWithText("خروجی داده").performClick()
        assert(openedExport)
    }

    @Test
    fun settings_postPregnancy_showsResumeActions() {
        var resumedCycle = false
        composeRule.setContent {
            MahinTheme {
                SettingsScreenContent(
                    state =
                        SettingsUiState(
                            reproductiveMode = ReproductiveMode.POST_PREGNANCY_TRANSITION,
                            postPregnancyTransition = true,
                        ),
                    callbacks =
                        SettingsScreenCallbacks(
                            onNavigateUp = {},
                            onModeSelected = {},
                            onOpenNotifications = {},
                            onOpenPrivacy = {},
                            onOpenHealthConnect = {},
                            onOpenAssistant = {},
                            onOpenDataExport = {},
                            onOpenHistory = {},
                            onResumeCycle = { resumedCycle = true },
                            onResumeTtc = {},
                        ),
                )
            }
        }
        composeRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasText("بازگشت به پیگیری پریود"))
        composeRule.onNodeWithText("بازگشت به پیگیری پریود").performClick()
        assert(resumedCycle)
    }

    @Test
    fun settings_healthConnectHiddenWhenFlagOff() {
        composeRule.setContent {
            MahinTheme {
                SettingsScreenContent(
                    state = SettingsUiState(healthConnectEntryVisible = false),
                    callbacks =
                        SettingsScreenCallbacks(
                            onNavigateUp = {},
                            onModeSelected = {},
                            onOpenNotifications = {},
                            onOpenPrivacy = {},
                            onOpenHealthConnect = {},
                            onOpenAssistant = {},
                            onOpenDataExport = {},
                            onOpenHistory = {},
                            onResumeCycle = {},
                            onResumeTtc = {},
                        ),
                )
            }
        }
        composeRule.onNodeWithText("Health Connect (اختیاری)").assertDoesNotExist()
    }
}
