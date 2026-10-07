package dev.mahin.android.cycle

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.ReproductiveMode
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
                                        dev.mahin.domain.cycle.CyclePredictionResult(
                                            algorithmVersion = "v1",
                                            confidence = dev.mahin.domain.cycle.PredictionConfidence.LOW,
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
}
