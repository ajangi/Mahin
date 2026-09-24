package dev.mahin.domain.cycle

import dev.mahin.core.model.CycleRegularity
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Deterministic calendar-method estimates. Not contraception or diagnosis.
 */
object CyclePredictionEngineV1 {
    private const val DEFAULT_CYCLE_LENGTH = 28
    private const val DEFAULT_LUTEAL_PHASE = 14
    private const val MIN_CYCLE_LENGTH = 21
    private const val MAX_CYCLE_LENGTH = 45

    fun predict(input: CyclePredictionInput): CyclePredictionResult {
        val algorithmVersion = CycleDomainModule.PREDICTION_ALGORITHM_VERSION
        val anchorStart =
            input.openPeriodStart
                ?: input.completedCycles.maxByOrNull { it.periodStart }?.periodStart
        if (anchorStart == null) {
            return CyclePredictionResult(
                algorithmVersion = algorithmVersion,
                confidence = PredictionConfidence.INSUFFICIENT_DATA,
                cycleDay = null,
                nextPeriod = null,
                fertileWindow = null,
                estimatedOvulation = null,
                insufficientDataReason = "no_period_anchor",
            )
        }

        val cycleLengths = deriveCycleLengths(input.completedCycles, input.openPeriodStart)
        val cycleLength =
            clampCycle(
                when {
                    cycleLengths.isNotEmpty() -> cycleLengths.average().roundToInt()
                    input.typicalCycleLengthDays != null -> input.typicalCycleLengthDays
                    else -> DEFAULT_CYCLE_LENGTH
                },
            )
        val cycleDay = (ChronoUnit.DAYS.between(anchorStart, input.today) + 1).toInt().coerceAtLeast(1)
        val variability = if (cycleLengths.size >= 2) cycleLengths.standardDeviation() else 0.0
        val widening = wideningDays(variability, input.regularity, cycleLengths.size)

        val nextPeriodStart = anchorStart.plusDays(cycleLength.toLong())
        val nextPeriod =
            DateRangeEstimate(
                earliest = nextPeriodStart.minusDays(widening.toLong()),
                latest = nextPeriodStart.plusDays(widening.toLong()),
            )

        val ovulationCenter = nextPeriodStart.minusDays(DEFAULT_LUTEAL_PHASE.toLong())
        val ovulationWindow = max(1, widening / 2)
        val ovulation =
            DateRangeEstimate(
                earliest = ovulationCenter.minusDays(ovulationWindow.toLong()),
                latest = ovulationCenter.plusDays(ovulationWindow.toLong()),
            )
        val fertile =
            DateRangeEstimate(
                earliest = ovulation.earliest.minusDays(5),
                latest = ovulation.latest.plusDays(1),
            )

        val confidence = confidenceLevel(cycleLengths.size, input.regularity, variability)
        val insufficientReason =
            if (confidence == PredictionConfidence.INSUFFICIENT_DATA) {
                "need_more_completed_cycles"
            } else {
                null
            }

        return CyclePredictionResult(
            algorithmVersion = algorithmVersion,
            confidence = confidence,
            cycleDay = cycleDay,
            nextPeriod = nextPeriod,
            fertileWindow = fertile,
            estimatedOvulation = ovulation,
            insufficientDataReason = insufficientReason,
        )
    }

    private fun deriveCycleLengths(
        completed: List<CompletedCycleSpan>,
        openStart: LocalDate?,
    ): List<Int> {
        val sorted = completed.sortedBy { it.periodStart }
        val lengths = mutableListOf<Int>()
        for (i in 0 until sorted.size - 1) {
            val days =
                ChronoUnit.DAYS.between(sorted[i].periodStart, sorted[i + 1].periodStart).toInt()
            if (days in MIN_CYCLE_LENGTH..MAX_CYCLE_LENGTH) {
                lengths.add(days)
            }
        }
        if (openStart != null && sorted.isNotEmpty()) {
            val last = sorted.last()
            if (last.periodStart.isBefore(openStart)) {
                val days = ChronoUnit.DAYS.between(last.periodStart, openStart).toInt()
                if (days in MIN_CYCLE_LENGTH..MAX_CYCLE_LENGTH) {
                    lengths.add(days)
                }
            }
        }
        return lengths
    }

    private fun clampCycle(value: Int): Int = value.coerceIn(MIN_CYCLE_LENGTH, MAX_CYCLE_LENGTH)

    private fun wideningDays(
        variability: Double,
        regularity: CycleRegularity,
        sampleCount: Int,
    ): Int {
        val base =
            when (regularity) {
                CycleRegularity.REGULAR -> 1
                CycleRegularity.SOMEWHAT_IRREGULAR -> 2
                CycleRegularity.IRREGULAR -> 4
                CycleRegularity.UNKNOWN -> 3
            }
        val sampleBonus =
            if (sampleCount < 2) {
                2
            } else if (sampleCount < 3) {
                1
            } else {
                0
            }
        val variabilityBonus = (variability / 2.0).roundToInt().coerceIn(0, 5)
        return (base + sampleBonus + variabilityBonus).coerceIn(1, 7)
    }

    private fun confidenceLevel(
        sampleCount: Int,
        regularity: CycleRegularity,
        variability: Double,
    ): PredictionConfidence =
        when {
            sampleCount == 0 && regularity == CycleRegularity.UNKNOWN ->
                PredictionConfidence.INSUFFICIENT_DATA
            sampleCount == 0 ||
                sampleCount == 1 ||
                regularity == CycleRegularity.IRREGULAR ||
                variability > 4.0 -> PredictionConfidence.LOW
            sampleCount >= 3 && regularity == CycleRegularity.REGULAR && variability <= 2.0 ->
                PredictionConfidence.HIGH
            else -> PredictionConfidence.MEDIUM
        }

    private fun List<Int>.standardDeviation(): Double {
        if (size < 2) return 0.0
        val mean = average()
        val variance = map { (it - mean) * (it - mean) }.average()
        return kotlin.math.sqrt(variance)
    }
}
