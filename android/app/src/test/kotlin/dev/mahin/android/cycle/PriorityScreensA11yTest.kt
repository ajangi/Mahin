package dev.mahin.android.cycle

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.LayoutDirection
import dev.mahin.android.onboarding.OnboardingGoalScreen
import dev.mahin.android.onboarding.OnboardingWelcomeScreen
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.ReproductiveMode
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private fun noopLogScreenActions(): LogScreenActions =
    LogScreenActions(
        onDateSelected = {},
        onToggleLoggingPeriod = {},
        onFlowLevelSelected = {},
        onToggleSymptom = {},
        onNoteChange = {},
        onSave = {},
        onTogglePregnancySymptom = {},
        onPregnancyWeightChange = {},
        onPregnancyBpSystolicChange = {},
        onPregnancyBpDiastolicChange = {},
        ttcCallbacks =
            TtcLogFormCallbacks(
                onBbtChange = {},
                onOvulationTestSelected = {},
                onCervicalMucusSelected = {},
                onIntercourseOptInChanged = {},
                onIntercourseToggle = {},
                onIntercourseProtectedSelected = {},
                onPregnancyTestSelected = {},
            ),
    )

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], manifest = Config.NONE, qualifiers = "fa-rIR")
class PriorityScreensA11yTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun logScreenList_exposedInRtl() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme {
                    LogScreenContent(
                        state =
                            LogUiState(
                                selectedJalali = JalaliDate(1403, 1, 1),
                            ),
                        actions = noopLogScreenActions(),
                    )
                }
            }
        }
        composeRule.onNodeWithTag("log_screen_list").assertIsDisplayed()
    }

    @Test
    fun todayScreenList_andHeading_exposedInRtl() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme {
                    TodayScreenContent(state = TodayUiState())
                }
            }
        }
        composeRule.onNodeWithTag("today_screen_list").assertIsDisplayed()
        val headingMatcher =
            SemanticsMatcher("has heading semantics") { node ->
                node.config.getOrNull(SemanticsProperties.Heading) != null
            }
        assertNotNull(composeRule.onNode(headingMatcher).fetchSemanticsNode())
    }

    @Test
    fun historyEmptyStateComposesInRtl() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme {
                    HistoryScreenContent(periods = emptyList())
                }
            }
        }
        composeRule.onNodeWithTag("history_screen_list").assertIsDisplayed()
    }

    @Test
    fun calendarScreenHasTestTagInRtl() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme {
                    CycleCalendarScreenContent(
                        state =
                            CalendarUiState(
                                selectedJalali = JalaliDate(1403, 6, 15),
                            ),
                        onDateSelected = {},
                    )
                }
            }
        }
        composeRule.onNodeWithTag("cycle_calendar_screen").assertIsDisplayed()
    }

    @Test
    fun onboardingWelcomeHasHeadingSemantics() {
        composeRule.setContent {
            MahinTheme {
                OnboardingWelcomeScreen(onContinue = {})
            }
        }
        val headingMatcher =
            SemanticsMatcher("has heading semantics") { node ->
                node.config.getOrNull(SemanticsProperties.Heading) != null
            }
        val headingNode = composeRule.onNode(headingMatcher).fetchSemanticsNode()
        assertNotNull(headingNode)
    }

    @Test
    fun onboardingGoalScreenComposesInRtl() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme {
                    OnboardingGoalScreen(
                        selectedMode = ReproductiveMode.CYCLE_TRACKING,
                        onModeSelected = {},
                        onContinue = {},
                    )
                }
            }
        }
        composeRule.onNodeWithTag("onboarding_goal_screen").assertIsDisplayed()
    }
}
