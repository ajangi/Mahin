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

data class PeriodSpanForSnapshot(
    val startDate: LocalDate,
    val endDate: LocalDate?,
)

data class CycleTodaySnapshotInput(
    val today: LocalDate,
    val prediction: CyclePredictionResult,
    val periodAnchorStart: LocalDate?,
    val currentPeriod: PeriodSpanForSnapshot?,
    val typicalPeriodLengthDays: Int?,
    val onPeriodToday: Boolean,
)

/**
 * Composes existing prediction output into the Today cycle hero model. No new prediction math.
 */
object TodaySnapshotUseCase {
    private const val DEFAULT_CYCLE_LENGTH = 28

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
        val cycleLength =
            estimateCycleLengthDays(
                periodAnchorStart = input.periodAnchorStart,
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
        val segments = buildRingSegments(input, cycleLength)
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
        periodAnchorStart: LocalDate?,
        nextPeriod: DateRangeEstimate?,
    ): Int {
        if (periodAnchorStart != null && nextPeriod != null) {
            return (ChronoUnit.DAYS.between(periodAnchorStart, nextPeriod.latest) + 1)
                .toInt()
                .coerceAtLeast(1)
        }
        return DEFAULT_CYCLE_LENGTH
    }

    private fun buildRingSegments(
        input: CycleTodaySnapshotInput,
        cycleLength: Int,
    ): List<CycleRingSegment> {
        val segments = mutableListOf<CycleRingSegment>()
        val anchor = input.periodAnchorStart
        input.currentPeriod?.let { period ->
            val loggedEnd =
                when {
                    period.endDate != null -> period.endDate
                    else -> input.today
                }
            calendarRangeToCycleDays(anchor, period.startDate, loggedEnd, cycleLength)?.let { (start, end) ->
                segments +=
                    CycleRingSegment(
                        kind = CycleRingSegmentKind.LOGGED_PERIOD,
                        startDay = start,
                        endDay = end,
                    )
            }
        }
        input.prediction.nextPeriod?.let { range ->
            calendarRangeToCycleDays(anchor, range.earliest, range.latest, cycleLength)?.let { (start, end) ->
                segments +=
                    CycleRingSegment(
                        kind = CycleRingSegmentKind.PREDICTED_PERIOD,
                        startDay = start,
                        endDay = end,
                    )
            }
        }
        input.prediction.fertileWindow?.let { range ->
            calendarRangeToCycleDays(anchor, range.earliest, range.latest, cycleLength)?.let { (start, end) ->
                segments +=
                    CycleRingSegment(
                        kind = CycleRingSegmentKind.FERTILE_WINDOW,
                        startDay = start,
                        endDay = end,
                    )
            }
        }
        input.prediction.estimatedOvulation?.let { range ->
            calendarRangeToCycleDays(anchor, range.earliest, range.latest, cycleLength)?.let { (start, end) ->
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

    private fun calendarRangeToCycleDays(
        anchor: LocalDate?,
        rangeStart: LocalDate,
        rangeEnd: LocalDate,
        cycleLength: Int,
    ): Pair<Int, Int>? {
        if (anchor == null) return null
        val start = (ChronoUnit.DAYS.between(anchor, rangeStart) + 1).toInt()
        val end = (ChronoUnit.DAYS.between(anchor, rangeEnd) + 1).toInt()
        if (end < 1 || start > cycleLength) return null
        return start.coerceAtLeast(1) to end.coerceAtMost(cycleLength)
    }
}
