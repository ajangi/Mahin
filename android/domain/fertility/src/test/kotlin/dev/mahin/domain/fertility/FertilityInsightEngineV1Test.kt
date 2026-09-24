package dev.mahin.domain.fertility

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.OvulationTestResult
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

    private fun prediction(cycleDay: Int = 12): CyclePredictionResult =
        CyclePredictionResult(
            algorithmVersion = "cycle-prediction-v1",
            confidence = PredictionConfidence.MEDIUM,
            cycleDay = cycleDay,
            nextPeriod = null,
            fertileWindow = fertileWindow,
            estimatedOvulation = DateRangeEstimate(LocalDate.of(2025, 3, 14), LocalDate.of(2025, 3, 15)),
            insufficientDataReason = null,
        )

    @Test
    fun buildInsight_doesNotNarrowFertileWindow() {
        val today = LocalDate.of(2025, 3, 13)
        val cycleStart = today.minusDays(11)
        val priorCycleOpk =
            TtcSignalDay(
                date = cycleStart.minusDays(10),
                bbtCelsius = null,
                ovulationTestResult = OvulationTestResult.PEAK,
                cervicalMucus = null,
                intercourseLogged = false,
                pregnancyTestResult = null,
            )
        val currentOpk =
            TtcSignalDay(
                date = today,
                bbtCelsius = null,
                ovulationTestResult = OvulationTestResult.POSITIVE,
                cervicalMucus = CervicalMucusType.EGG_WHITE,
                intercourseLogged = false,
                pregnancyTestResult = null,
            )
        val result =
            FertilityInsightEngineV1.buildInsight(
                prediction = prediction(),
                signals = listOf(priorCycleOpk, currentOpk),
                currentCycleStart = cycleStart,
            )
        assertThat(result.cyclePrediction.fertileWindow).isEqualTo(fertileWindow)
        assertThat(result.ovulationTestSurgeDates).containsExactly(today)
        assertThat(result.fertileEggWhiteDates).containsExactly(today)
    }

    @Test
    fun buildInsight_excludesSignalsBeforeCurrentCycleStart() {
        val today = LocalDate.of(2025, 3, 20)
        val cycleStart = LocalDate.of(2025, 3, 1)
        val oldSignal =
            TtcSignalDay(
                date = LocalDate.of(2025, 2, 15),
                bbtCelsius = null,
                ovulationTestResult = OvulationTestResult.PEAK,
                cervicalMucus = null,
                intercourseLogged = false,
                pregnancyTestResult = null,
            )
        val result =
            FertilityInsightEngineV1.buildInsight(
                prediction = prediction(cycleDay = 20),
                signals = listOf(oldSignal),
                currentCycleStart = cycleStart,
            )
        assertThat(result.ovulationTestSurgeDates).isEmpty()
    }

    @Test
    fun bbtShift_detectedOnlyInCurrentCycleSegment() {
        val cycleStart = LocalDate.of(2025, 3, 1)
        val signals =
            (0 until 9).map { offset ->
                val date = cycleStart.plusDays(offset.toLong())
                val temp = if (offset < 6) 36.4 else 36.7
                TtcSignalDay(
                    date = date,
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
                currentCycleStart = cycleStart,
            )
        assertThat(result.bbtShiftSuggestedDate).isEqualTo(cycleStart.plusDays(6))
    }

    @Test
    fun currentCycleStart_usesOpenPeriodAnchorWhenProvided() {
        val anchor = LocalDate.of(2025, 2, 1)
        val start =
            FertilityInsightEngineV1.currentCycleStart(
                prediction = prediction(cycleDay = 5),
                today = LocalDate.of(2025, 2, 10),
                periodAnchorStart = anchor,
            )
        assertThat(start).isEqualTo(anchor)
    }
}
