package dev.mahin.android.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.ReproductiveMode
import org.junit.Assert.assertEquals
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
    fun settings_entryCallbacks_fireForNotificationsPrivacyHistoryIntegrationsAssistant() {
        var openedNotifications = false
        var openedPrivacy = false
        var openedHistory = false
        var openedHealthConnect = false
        var openedAssistant = false
        composeRule.setContent {
            MahinTheme {
                SettingsScreenContent(
                    state =
                        SettingsUiState(
                            healthConnectEntryVisible = true,
                            healthAssistantEntryVisible = true,
                        ),
                    callbacks =
                        noopCallbacks(
                            onOpenNotifications = { openedNotifications = true },
                            onOpenPrivacy = { openedPrivacy = true },
                            onOpenHistory = { openedHistory = true },
                            onOpenHealthConnect = { openedHealthConnect = true },
                            onOpenAssistant = { openedAssistant = true },
                        ),
                )
            }
        }
        composeRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasText("اعلان‌ها"))
        composeRule.onNodeWithText("تنظیمات یادآوری و حریم اعلان").performClick()
        assertTrue(openedNotifications)

        composeRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasText("حریم خصوصی و امنیت"))
        composeRule
            .onNode(hasText("حریم خصوصی و امنیت") and hasClickAction())
            .performClick()
        assertTrue(openedPrivacy)

        composeRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasText("تاریخچهٔ پریود"))
        composeRule.onNodeWithText("تاریخچهٔ پریود").performClick()
        assertTrue(openedHistory)

        composeRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasText("Health Connect (اختیاری)"))
        composeRule.onNodeWithText("Health Connect (اختیاری)").performClick()
        assertTrue(openedHealthConnect)

        composeRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasText("دستیار آموزشی (آزمایشی)"))
        composeRule.onNodeWithText("دستیار آموزشی (آزمایشی)").performClick()
        assertTrue(openedAssistant)
    }

    @Test
    fun settings_modeSelection_invokesCallback() {
        var selected: ReproductiveMode? = null
        composeRule.setContent {
            MahinTheme {
                SettingsScreenContent(
                    state = SettingsUiState(reproductiveMode = ReproductiveMode.CYCLE_TRACKING),
                    callbacks = noopCallbacks(onModeSelected = { selected = it }),
                )
            }
        }
        composeRule.onNodeWithText("قصد بارداری دارم").performClick()
        assertEquals(ReproductiveMode.TRYING_TO_CONCEIVE, selected)
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
    fun settings_premiumActive_showsActiveLabel() {
        composeRule.setContent {
            MahinTheme {
                SettingsScreenContent(
                    state = SettingsUiState(premiumActive = true, showPremiumPaywallEntry = false),
                    callbacks = noopCallbacks(),
                )
            }
        }
        composeRule.onNodeWithTag("settings_screen_list").performScrollToNode(hasText("ماهین پریمیوم"))
        composeRule.onNodeWithText("پریمیوم فعال است").assertIsDisplayed()
        composeRule.onNode(hasText("پریمیوم فعال است") and hasClickAction()).assertDoesNotExist()
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
