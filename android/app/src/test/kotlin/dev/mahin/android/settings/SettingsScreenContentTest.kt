package dev.mahin.android.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.ReproductiveMode
import org.junit.Assert.assertTrue
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
    fun settings_showsNotificationPrivacyHistoryRows() {
        composeRule.setContent {
            MahinTheme {
                SettingsScreenContent(
                    state = SettingsUiState(),
                    callbacks = noopCallbacks(),
                )
            }
        }
        composeRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasText("اعلان‌ها"))
        composeRule.onNodeWithText("تنظیمات یادآوری و حریم اعلان").assertIsDisplayed()
        composeRule.onNodeWithText("تاریخچهٔ پریود").assertIsDisplayed()
    }

    @Test
    fun settings_showsRelocatedExportEntry() {
        var openedExport = false
        composeRule.setContent {
            MahinTheme {
                SettingsScreenContent(
                    state = SettingsUiState(healthConnectEntryVisible = true, healthAssistantEntryVisible = true),
                    callbacks = noopCallbacks(onOpenDataExport = { openedExport = true }),
                )
            }
        }
        composeRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasText("خروجی داده"))
        composeRule.onNodeWithText("خروجی داده").performClick()
        assertTrue(openedExport)
    }

    @Test
    fun settings_healthConnectVisibleWhenFlagOn() {
        composeRule.setContent {
            MahinTheme {
                SettingsScreenContent(
                    state = SettingsUiState(healthConnectEntryVisible = true),
                    callbacks = noopCallbacks(),
                )
            }
        }
        composeRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasText("اتصال‌ها"))
        composeRule.onNodeWithText("Health Connect (اختیاری)").assertIsDisplayed()
    }

    @Test
    fun settings_healthConnectHiddenWhenFlagOff() {
        composeRule.setContent {
            MahinTheme {
                SettingsScreenContent(
                    state = SettingsUiState(healthConnectEntryVisible = false),
                    callbacks = noopCallbacks(),
                )
            }
        }
        composeRule.onNodeWithText("Health Connect (اختیاری)").assertDoesNotExist()
    }

    @Test
    fun settings_assistantVisibleWhenFlagOn() {
        composeRule.setContent {
            MahinTheme {
                SettingsScreenContent(
                    state = SettingsUiState(healthAssistantEntryVisible = true),
                    callbacks = noopCallbacks(),
                )
            }
        }
        composeRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasText("دستیار"))
        composeRule.onNodeWithText("دستیار آموزشی (آزمایشی)").assertIsDisplayed()
    }

    @Test
    fun settings_assistantHiddenWhenFlagOff() {
        composeRule.setContent {
            MahinTheme {
                SettingsScreenContent(
                    state = SettingsUiState(healthAssistantEntryVisible = false),
                    callbacks = noopCallbacks(),
                )
            }
        }
        composeRule.onNodeWithText("دستیار آموزشی (آزمایشی)").assertDoesNotExist()
    }

    @Test
    fun settings_premiumRow_invokesCallback() {
        var opened = false
        composeRule.setContent {
            MahinTheme {
                SettingsScreenContent(
                    state = SettingsUiState(),
                    callbacks = noopCallbacks(onOpenPremium = { opened = true }),
                )
            }
        }
        composeRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasText("ماهین پریمیوم"))
        composeRule.onNodeWithText("مشاهدهٔ پلن پریمیوم").performClick()
        assertTrue(opened)
    }

    @Test
    fun settings_modeBlockedMessage_visible() {
        composeRule.setContent {
            MahinTheme {
                SettingsScreenContent(
                    state = SettingsUiState(modeChangeBlockedMessage = true),
                    callbacks = noopCallbacks(),
                )
            }
        }
        composeRule.onNodeWithText("برای تغییر حالت، ابتدا نتیجهٔ بارداری را ثبت کنید.").assertIsDisplayed()
    }

    @Test
    fun settings_postPregnancy_showsResumeActions() {
        var resumedCycle = false
        var resumedTtc = false
        composeRule.setContent {
            MahinTheme {
                SettingsScreenContent(
                    state =
                        SettingsUiState(
                            reproductiveMode = ReproductiveMode.POST_PREGNANCY_TRANSITION,
                            postPregnancyTransition = true,
                        ),
                    callbacks =
                        noopCallbacks(
                            onResumeCycle = { resumedCycle = true },
                            onResumeTtc = { resumedTtc = true },
                        ),
                )
            }
        }
        composeRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasText("بازگشت به پیگیری پریود"))
        composeRule.onNodeWithTag("settings_resume_cycle").performClick()
        composeRule.waitForIdle()
        assertTrue(resumedCycle)
        composeRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasText("بازگشت به قصد بارداری"))
        composeRule.onNodeWithTag("settings_resume_ttc").performClick()
        composeRule.waitForIdle()
        assertTrue(resumedTtc)
    }

    @Suppress("LongParameterList")
    private fun noopCallbacks(
        onOpenNotifications: () -> Unit = {},
        onOpenPrivacy: () -> Unit = {},
        onOpenHealthConnect: () -> Unit = {},
        onOpenAssistant: () -> Unit = {},
        onOpenDataExport: () -> Unit = {},
        onOpenPremium: () -> Unit = {},
        onOpenHistory: () -> Unit = {},
        onModeSelected: (ReproductiveMode) -> Unit = {},
        onResumeCycle: () -> Unit = {},
        onResumeTtc: () -> Unit = {},
    ): SettingsScreenCallbacks =
        SettingsScreenCallbacks(
            onNavigateUp = {},
            onModeSelected = onModeSelected,
            onOpenNotifications = onOpenNotifications,
            onOpenPrivacy = onOpenPrivacy,
            onOpenHealthConnect = onOpenHealthConnect,
            onOpenAssistant = onOpenAssistant,
            onOpenDataExport = onOpenDataExport,
            onOpenPremium = onOpenPremium,
            onOpenHistory = onOpenHistory,
            onResumeCycle = onResumeCycle,
            onResumeTtc = onResumeTtc,
        )
}
