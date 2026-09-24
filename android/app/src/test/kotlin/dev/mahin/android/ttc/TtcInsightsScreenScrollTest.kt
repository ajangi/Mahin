package dev.mahin.android.ttc

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import dev.mahin.core.database.entity.TtcDayLogEntity
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.domain.cycle.CyclePredictionResult
import dev.mahin.domain.cycle.DateRangeEstimate
import dev.mahin.domain.cycle.PredictionConfidence
import dev.mahin.domain.fertility.FertilityInsightEngineV1
import dev.mahin.domain.fertility.FertilityInsightResult
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], manifest = Config.NONE, qualifiers = "fa-rIR")
class TtcInsightsScreenScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun ttcInsightsScreen_largeFontScale_showsLastTimelineRowAndDisclaimer() {
        val prediction =
            CyclePredictionResult(
                algorithmVersion = "cycle-prediction-v1",
                confidence = PredictionConfidence.MEDIUM,
                cycleDay = 10,
                nextPeriod = null,
                fertileWindow =
                    DateRangeEstimate(
                        LocalDate.of(2025, 3, 10),
                        LocalDate.of(2025, 3, 16),
                    ),
                estimatedOvulation = null,
                insufficientDataReason = null,
            )
        val insight =
            FertilityInsightResult(
                algorithmVersion = FertilityInsightEngineV1.ALGORITHM_VERSION,
                cyclePrediction = prediction,
                ovulationTestSurgeDates = emptyList(),
                bbtShiftSuggestedDate = null,
                fertileEggWhiteDates = emptyList(),
            )
        val timeline =
            (0 until 12).map { index ->
                TtcDayLogEntity(
                    id = "t$index",
                    logDate = LocalDate.of(2025, 3, 1).plusDays(index.toLong()),
                    bbtCelsius = 36.5 + index * 0.01,
                    ovulationTestResult = null,
                    cervicalMucus = null,
                    intercourseLogged = false,
                    intercourseProtected = null,
                    pregnancyTestResult = null,
                    updatedAtEpochMs = 0L,
                )
            }
        val disclaimer =
            "این اطلاعات تخمینی است؛ تضمین بارداری یا روش ضدبارداری نیست. برای تصمیم پزشکی با متخصص مشورت کنید."
        composeRule.setContent {
            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl,
                LocalDensity provides Density(density = 1f, fontScale = 2f),
            ) {
                MahinTheme {
                    TtcInsightsScreenContent(
                        state =
                            TtcInsightsContentState(
                                isLoading = false,
                                isTtcMode = true,
                                intercourseLoggingEnabled = false,
                                insight = insight,
                                timeline = timeline,
                                bbtPoints = timeline.mapNotNull { BbtChartPoint(it.logDate, it.bbtCelsius!!) },
                            ),
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val lastTimelineIndex = 4 + timeline.lastIndex
        composeRule.onNodeWithTag("ttc_insights_list").performScrollToIndex(lastTimelineIndex)
        composeRule.onNodeWithText("۳۶٫۶۱", substring = true).assertIsDisplayed()
        composeRule
            .onNodeWithTag("ttc_insights_list")
            .performScrollToNode(hasText(disclaimer, substring = true))
        composeRule.onNodeWithText(disclaimer, substring = true).assertIsDisplayed()
    }
}
