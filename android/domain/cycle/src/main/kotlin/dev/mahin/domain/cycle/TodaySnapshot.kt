package dev.mahin.domain.cycle

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class CycleRingSegmentKind {
    LOGGED_PERIOD,
    PREDICTED_PERIOD,
    FERTILE_WINDOW,
    ESTIMATED_OVULATION,
}

data class CycleRingSegment(
    val kind: CycleRingSegmentKind,
    /** Inclusive 1-based cycle day. */
    val startDay: Int,
    val endDay: Int,
)

data class CycleTodayHero(
    val cycleDay: Int?,
    val cycleLengthDays: Int,
    val ringSegments: List<CycleRingSegment>,
    val confidence: PredictionConfidence,
    val showConfidenceChip: Boolean,
    /** Days until the earliest estimated next period; negative when overdue. */
    val daysUntilNextPeriodEarliest: Int?,
    val isInFertileWindow: Boolean,
    val isOverdue: Boolean,
)

sealed interface TodaySnapshot {
    data object FirstDay : TodaySnapshot

    data class Cycle(
        val hero: CycleTodayHero,
    ) : TodaySnapshot
}

data class CycleTodaySnapshotInput(
    val today: LocalDate,
    val prediction: CyclePredictionResult,
    val periodAnchorStart: LocalDate?,
    val typicalPeriodLengthDays: Int?,
    val onPeriodToday: Boolean,
)

/**
 * Composes existing prediction output into the Today cycle hero model. No new prediction math.
 */
object TodaySnapshotUseCase {
    private const val DEFAULT_CYCLE_LENGTH = 28
    private const val DEFAULT_PERIOD_LENGTH = 5

    fun fromCycle(input: CycleTodaySnapshotInput): TodaySnapshot {
        if (input.periodAnchorStart == null && input.prediction.cycleDay == null) {
            return TodaySnapshot.FirstDay
        }
        val hero = buildCycleHero(input)
        return TodaySnapshot.Cycle(hero = hero)
    }

    private fun buildCycleHero(input: CycleTodaySnapshotInput): CycleTodayHero {
        val prediction = input.prediction
        val cycleDay = prediction.cycleDay
        val periodLength = (input.typicalPeriodLengthDays ?: DEFAULT_PERIOD_LENGTH).coerceAtLeast(1)
        val cycleLength =
            estimateCycleLengthDays(
                anchor = input.periodAnchorStart,
                today = input.today,
                cycleDay = cycleDay,
                nextPeriod = prediction.nextPeriod,
            )
        val daysUntil =
            prediction.nextPeriod?.let { range ->
                ChronoUnit.DAYS.between(input.today, range.earliest).toInt()
            }
        val overdue =
            prediction.nextPeriod?.let { range ->
                !input.onPeriodToday && input.today.isAfter(range.latest)
            } == true
        val inFertile =
            prediction.fertileWindow?.let { range ->
                !input.today.isBefore(range.earliest) && !input.today.isAfter(range.latest)
            } == true
        val segments = buildRingSegments(input, cycleLength, periodLength)
        val showChip =
            prediction.confidence == PredictionConfidence.LOW ||
                prediction.confidence == PredictionConfidence.INSUFFICIENT_DATA
        return CycleTodayHero(
            cycleDay = cycleDay,
            cycleLengthDays = cycleLength,
            ringSegments = segments,
            confidence = prediction.confidence,
            showConfidenceChip = showChip,
            daysUntilNextPeriodEarliest = daysUntil,
            isInFertileWindow = inFertile,
            isOverdue = overdue,
        )
    }

    private fun estimateCycleLengthDays(
        anchor: LocalDate?,
        today: LocalDate,
        cycleDay: Int?,
        nextPeriod: DateRangeEstimate?,
    ): Int {
        if (anchor != null && cycleDay != null && nextPeriod != null) {
            val daysToEarliest = ChronoUnit.DAYS.between(today, nextPeriod.earliest).toInt()
            return (cycleDay + daysToEarliest).coerceIn(21, 45)
        }
        return DEFAULT_CYCLE_LENGTH
    }

    private fun buildRingSegments(
        input: CycleTodaySnapshotInput,
        cycleLength: Int,
        periodLength: Int,
    ): List<CycleRingSegment> {
        val segments = mutableListOf<CycleRingSegment>()
        val loggedEnd = periodLength.coerceAtMost(cycleLength)
        segments +=
            CycleRingSegment(
                kind = CycleRingSegmentKind.LOGGED_PERIOD,
                startDay = 1,
                endDay = loggedEnd,
            )
        val predictedStart = (cycleLength - periodLength + 1).coerceAtLeast(1)
        segments +=
            CycleRingSegment(
                kind = CycleRingSegmentKind.PREDICTED_PERIOD,
                startDay = predictedStart,
                endDay = cycleLength,
            )
        input.prediction.fertileWindow?.let { range ->
            dateRangeToCycleDays(input.periodAnchorStart, range, cycleLength)?.let { (start, end) ->
                segments +=
                    CycleRingSegment(
                        kind = CycleRingSegmentKind.FERTILE_WINDOW,
                        startDay = start,
                        endDay = end,
                    )
            }
        }
        input.prediction.estimatedOvulation?.let { range ->
            dateRangeToCycleDays(input.periodAnchorStart, range, cycleLength)?.let { (start, end) ->
                segments +=
                    CycleRingSegment(
                        kind = CycleRingSegmentKind.ESTIMATED_OVULATION,
                        startDay = start,
                        endDay = end,
                    )
            }
        }
        return segments
    }

    private fun dateRangeToCycleDays(
        anchor: LocalDate?,
        range: DateRangeEstimate,
        cycleLength: Int,
    ): Pair<Int, Int>? {
        if (anchor == null) return null
        val start = (ChronoUnit.DAYS.between(anchor, range.earliest) + 1).toInt()
        val end = (ChronoUnit.DAYS.between(anchor, range.latest) + 1).toInt()
        if (end < 1 || start > cycleLength) return null
        return start.coerceAtLeast(1) to end.coerceAtMost(cycleLength)
    }
}
