package dev.mahin.android.assistant

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.LayoutDirection
import dev.mahin.core.designsystem.MahinTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], manifest = Config.NONE, qualifiers = "fa-rIR")
class AssistantSettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun switchesDefaultOffAndHaveSemantics() {
        composeRule.setContent {
            MahinTheme {
                AssistantSettingsScreenContent(
                    state =
                        AssistantSettingsUiState(
                            launchFlagLoading = false,
                            launchFlagEnabled = true,
                        ),
                    onNavigateUp = {},
                    onShareCycleSummary = {},
                    onShareSymptomTags = {},
                    onSaveConsent = {},
                )
            }
        }
        composeRule.onNodeWithTag("assistant_consent_cycle_summary").assertIsOff()
        composeRule.onNodeWithTag("assistant_consent_symptom_tags").assertIsOff()
        val cycleNode = composeRule.onNodeWithTag("assistant_consent_cycle_summary").fetchSemanticsNode()
        assertTrue(cycleNode.config.contains(SemanticsProperties.ContentDescription))
        assertTrue(cycleNode.config.contains(SemanticsProperties.ToggleableState))
    }

    @Test
    fun rtlLayoutComposes() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme {
                    AssistantSettingsScreenContent(
                        state =
                            AssistantSettingsUiState(
                                launchFlagLoading = false,
                                launchFlagEnabled = true,
                            ),
                        onNavigateUp = {},
                        onShareCycleSummary = {},
                        onShareSymptomTags = {},
                        onSaveConsent = {},
                    )
                }
            }
        }
        composeRule.onNodeWithTag("assistant_settings_screen").assertIsDisplayed()
    }

    @Test
    fun flagOffRendersNothing() {
        composeRule.setContent {
            MahinTheme {
                AssistantSettingsScreenContent(
                    state =
                        AssistantSettingsUiState(
                            launchFlagLoading = false,
                            launchFlagEnabled = false,
                        ),
                    onNavigateUp = {},
                    onShareCycleSummary = {},
                    onShareSymptomTags = {},
                    onSaveConsent = {},
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("assistant_settings_screen").assertDoesNotExist()
    }
}
