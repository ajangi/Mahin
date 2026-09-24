package dev.mahin.domain.fertility

import dev.mahin.domain.cycle.CyclePredictionResult
import dev.mahin.domain.cycle.DateRangeEstimate
import java.time.LocalDate

/**
 * Combines calendar-method cycle estimates with user-logged TTC signals.
 * Outputs are indicative only — not contraception or diagnosis.
 */
object FertilityInsightEngineV1 {
    const val ALGORITHM_VERSION: String = "fertility-insight-v1"
    private const val BBT_SHIFT_DELTA_CELSIUS = 0.2
    private const val BBT_BASELINE_DAYS = 6
    private const val BBT_HIGH_DAYS = 3

    fun buildInsight(
        prediction: CyclePredictionResult,
        signals: List<TtcSignalDay>,
        @Suppress("UNUSED_PARAMETER") today: LocalDate,
    ): FertilityInsightResult {
        val sorted = signals.sortedBy { it.date }
        val surgeDates =
            sorted
                .filter { day ->
                    day.ovulationTestResult == "PEAK" || day.ovulationTestResult == "POSITIVE"
                }.map { it.date }
        val eggWhiteDates =
            sorted
                .filter { it.cervicalMucus == "EGG_WHITE" }
                .map { it.date }
        val bbtShift = detectBbtShift(sorted)

        val fertile = prediction.fertileWindow
        val aligned =
            fertile != null &&
                surgeDates.any { date -> !date.isBefore(fertile.earliest) && !date.isAfter(fertile.latest) }

        val highlighted =
            when {
                fertile == null -> null
                surgeDates.isEmpty() && bbtShift == null -> fertile
                else -> narrowWindow(fertile, surgeDates, bbtShift, eggWhiteDates)
            }

        return FertilityInsightResult(
            algorithmVersion = ALGORITHM_VERSION,
            cyclePrediction = prediction,
            ovulationTestSurgeDates = surgeDates,
            bbtShiftSuggestedDate = bbtShift,
            fertileEggWhiteDates = eggWhiteDates,
            signalAlignedWithEstimate = aligned,
            highlightedFertileWindow = highlighted,
        )
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

    private fun narrowWindow(
        base: DateRangeEstimate,
        surgeDates: List<LocalDate>,
        bbtShift: LocalDate?,
        eggWhiteDates: List<LocalDate>,
    ): DateRangeEstimate {
        val anchors = surgeDates + listOfNotNull(bbtShift) + eggWhiteDates
        if (anchors.isEmpty()) return base
        val earliest = anchors.minOrNull() ?: base.earliest
        val latest = anchors.maxOrNull() ?: base.latest
        val narrowedEarliest = maxOf(base.earliest, earliest.minusDays(1))
        val narrowedLatest = minOf(base.latest, latest.plusDays(1))
        if (narrowedEarliest.isAfter(narrowedLatest)) return base
        return DateRangeEstimate(earliest = narrowedEarliest, latest = narrowedLatest)
    }
}
