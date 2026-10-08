package dev.mahin.android.cycle

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.mahin.android.pregnancy.PostPregnancyTransitionActions
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.cycle.CyclePredictionResult
import dev.mahin.domain.cycle.PredictionConfidence
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR")
class TodayScreenContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun today_doesNotShowSettingsGroupOrModeCard() {
        composeRule.setContent {
            MahinTheme {
                TodayScreenContent(state = TodayUiState(reproductiveMode = ReproductiveMode.CYCLE_TRACKING))
            }
        }
        composeRule.onNodeWithText("تنظیمات و داده").assertDoesNotExist()
        composeRule.onNodeWithText("هدف ردیابی").assertDoesNotExist()
    }

    @Test
    fun today_ttcMode_showsTtcHint() {
        composeRule.setContent {
            MahinTheme {
                TodayScreenContent(
                    state =
                        TodayUiState(
                            reproductiveMode = ReproductiveMode.TRYING_TO_CONCEIVE,
                            dashboard =
                                dev.mahin.core.database.cycle.CycleDashboard(
                                    profile = null,
                                    prediction =
                                        CyclePredictionResult(
                                            algorithmVersion = "v1",
                                            confidence = PredictionConfidence.LOW,
                                            cycleDay = 1,
                                            nextPeriod = null,
                                            fertileWindow = null,
                                            estimatedOvulation = null,
                                            insufficientDataReason = null,
                                        ),
                                    todayLog = null,
                                    onPeriodToday = false,
                                    openPeriodStart = null,
                                ),
                        ),
                )
            }
        }
        composeRule.onNodeWithText("BBT", substring = true).assertExists()
    }

    @Test
    fun today_postPregnancyTransition_showsResumeActions() {
        var resumedCycle = false
        composeRule.setContent {
            MahinTheme {
                TodayScreenContent(
                    state =
                        TodayUiState(
                            reproductiveMode = ReproductiveMode.POST_PREGNANCY_TRANSITION,
                            postPregnancyTransition = true,
                        ),
                    postPregnancyActions =
                        PostPregnancyTransitionActions(
                            onResumeCycle = { resumedCycle = true },
                            onResumeTtc = {},
                        ),
                )
            }
        }
        composeRule.onNodeWithTag("post_pregnancy_transition_section").assertExists()
        composeRule.onNodeWithTag("post_pregnancy_resume_cycle").performClick()
        assertTrue(resumedCycle)
    }

    @Test
    fun today_postTransition_showsHistory_notCalendar_learnHiddenWhenFlagOff() {
        composeRule.setContent {
            MahinTheme {
                TodayScreenContent(
                    state =
                        TodayUiState(
                            reproductiveMode = ReproductiveMode.POST_PREGNANCY_TRANSITION,
                            postPregnancyTransition = true,
                            postTransitionLearnLinkVisible = false,
                        ),
                    postPregnancyActions =
                        PostPregnancyTransitionActions(
                            onResumeCycle = {},
                            onResumeTtc = {},
                        ),
                    onOpenHistory = {},
                )
            }
        }
        composeRule.onNodeWithText("چرخه‌های گذشته").assertExists()
        composeRule.onNodeWithText("تقویم چرخه").assertDoesNotExist()
        composeRule.onNodeWithText("آموزش").assertDoesNotExist()
    }

    @Test
    fun today_postTransition_showsLearnLinkWhenFlagOn() {
        composeRule.setContent {
            MahinTheme {
                TodayScreenContent(
                    state =
                        TodayUiState(
                            reproductiveMode = ReproductiveMode.POST_PREGNANCY_TRANSITION,
                            postPregnancyTransition = true,
                            postTransitionLearnLinkVisible = true,
                        ),
                    postPregnancyActions =
                        PostPregnancyTransitionActions(
                            onResumeCycle = {},
                            onResumeTtc = {},
                        ),
                    onOpenHistory = {},
                    onOpenLearn = {},
                )
            }
        }
        composeRule.onNodeWithText("آموزش").assertExists()
        composeRule.onNodeWithText("تقویم چرخه").assertDoesNotExist()
    }
}
