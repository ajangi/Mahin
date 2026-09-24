package dev.mahin.domain.fertility

import com.google.common.truth.Truth.assertThat
import dev.mahin.domain.cycle.CyclePredictionResult
import dev.mahin.domain.cycle.DateRangeEstimate
import dev.mahin.domain.cycle.PredictionConfidence
import java.time.LocalDate
import org.junit.Test

class FertilityInsightEngineV1Test {
    private val fertileWindow =
        DateRangeEstimate(
            earliest = LocalDate.of(2025, 3, 10),
            latest = LocalDate.of(2025, 3, 16),
        )

    private fun prediction(): CyclePredictionResult =
        CyclePredictionResult(
            algorithmVersion = "cycle-prediction-v1",
            confidence = PredictionConfidence.MEDIUM,
            cycleDay = 12,
            nextPeriod = null,
            fertileWindow = fertileWindow,
            estimatedOvulation = DateRangeEstimate(LocalDate.of(2025, 3, 14), LocalDate.of(2025, 3, 15)),
            insufficientDataReason = null,
        )

    @Test
    fun opkPeakInsideFertileWindow_marksAligned() {
        val signals =
            listOf(
                TtcSignalDay(
                    date = LocalDate.of(2025, 3, 13),
                    bbtCelsius = null,
                    ovulationTestResult = "PEAK",
                    cervicalMucus = null,
                    intercourseLogged = false,
                    pregnancyTestResult = null,
                ),
            )
        val result =
            FertilityInsightEngineV1.buildInsight(
                prediction = prediction(),
                signals = signals,
                today = LocalDate.of(2025, 3, 13),
            )
        assertThat(result.signalAlignedWithEstimate).isTrue()
        assertThat(result.ovulationTestSurgeDates).containsExactly(LocalDate.of(2025, 3, 13))
    }

    @Test
    fun bbtShift_detectedAfterBaseline() {
        val start = LocalDate.of(2025, 3, 1)
        val signals =
            (0 until 9).map { offset ->
                val temp =
                    when {
                        offset < 6 -> 36.4
                        else -> 36.7
                    }
                TtcSignalDay(
                    date = start.plusDays(offset.toLong()),
                    bbtCelsius = temp,
                    ovulationTestResult = null,
                    cervicalMucus = null,
                    intercourseLogged = false,
                    pregnancyTestResult = null,
                )
            }
        val result =
            FertilityInsightEngineV1.buildInsight(
                prediction = prediction(),
                signals = signals,
                today = start.plusDays(8),
            )
        assertThat(result.bbtShiftSuggestedDate).isEqualTo(start.plusDays(6))
    }

    @Test
    fun insufficientPrediction_stillReturnsSignalsWithoutHighlight() {
        val insufficient =
            prediction().copy(
                fertileWindow = null,
                confidence = PredictionConfidence.INSUFFICIENT_DATA,
            )
        val signals =
            listOf(
                TtcSignalDay(
                    date = LocalDate.of(2025, 3, 1),
                    bbtCelsius = null,
                    ovulationTestResult = "POSITIVE",
                    cervicalMucus = "EGG_WHITE",
                    intercourseLogged = true,
                    pregnancyTestResult = null,
                ),
            )
        val result =
            FertilityInsightEngineV1.buildInsight(
                prediction = insufficient,
                signals = signals,
                today = LocalDate.of(2025, 3, 1),
            )
        assertThat(result.highlightedFertileWindow).isNull()
        assertThat(result.fertileEggWhiteDates).isNotEmpty()
    }
}
