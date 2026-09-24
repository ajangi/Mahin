package dev.mahin.domain.fertility

import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.domain.cycle.CyclePredictionResult
import java.time.LocalDate

/**
 * Surfaces cycle estimates unchanged and lists user-logged TTC signals for the current cycle only.
 * Does not narrow prediction windows or infer medical conclusions.
 */
object FertilityInsightEngineV1 {
    const val ALGORITHM_VERSION: String = "fertility-insight-v2"
    private const val BBT_SHIFT_DELTA_CELSIUS = 0.2
    private const val BBT_BASELINE_DAYS = 6
    private const val BBT_HIGH_DAYS = 3

    fun buildInsight(
        prediction: CyclePredictionResult,
        signals: List<TtcSignalDay>,
        currentCycleStart: LocalDate?,
    ): FertilityInsightResult {
        val cycleSignals =
            if (currentCycleStart != null) {
                signals.filter { !it.date.isBefore(currentCycleStart) }
            } else {
                emptyList()
            }
        val sorted = cycleSignals.sortedBy { it.date }
        val surgeDates =
            sorted
                .filter { day ->
                    day.ovulationTestResult == OvulationTestResult.POSITIVE ||
                        day.ovulationTestResult == OvulationTestResult.PEAK
                }.map { it.date }
        val eggWhiteDates =
            sorted
                .filter { it.cervicalMucus == CervicalMucusType.EGG_WHITE }
                .map { it.date }
        val bbtShift = detectBbtShift(sorted)

        return FertilityInsightResult(
            algorithmVersion = ALGORITHM_VERSION,
            cyclePrediction = prediction,
            ovulationTestSurgeDates = surgeDates,
            bbtShiftSuggestedDate = bbtShift,
            fertileEggWhiteDates = eggWhiteDates,
        )
    }

    fun currentCycleStart(
        prediction: CyclePredictionResult,
        today: LocalDate,
        periodAnchorStart: LocalDate?,
    ): LocalDate? {
        periodAnchorStart?.let { return it }
        val cycleDay = prediction.cycleDay ?: return null
        if (cycleDay < 1) return null
        return today.minusDays((cycleDay - 1).toLong())
    }

    private fun detectBbtShift(sorted: List<TtcSignalDay>): LocalDate? {
        val withBbt = sorted.filter { it.bbtCelsius != null }
        if (withBbt.size < BBT_BASELINE_DAYS + BBT_HIGH_DAYS) return null
        for (index in BBT_BASELINE_DAYS until withBbt.size - BBT_HIGH_DAYS + 1) {
            val baseline = withBbt.subList(index - BBT_BASELINE_DAYS, index).mapNotNull { it.bbtCelsius }
            if (baseline.size < BBT_BASELINE_DAYS) continue
            val baselineAvg = baseline.average()
            val highSegment = withBbt.subList(index, index + BBT_HIGH_DAYS)
            val allHigh =
                highSegment.all { day ->
                    val value = day.bbtCelsius ?: return@all false
                    value >= baselineAvg + BBT_SHIFT_DELTA_CELSIUS
                }
            if (allHigh) {
                return highSegment.first().date
            }
        }
        return null
    }
}
