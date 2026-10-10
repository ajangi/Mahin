package dev.mahin.domain.cycle

import java.time.LocalDate

/**
 * Pure day-marker flags for calendar cells and day sheets. Shared by Today and Calendar surfaces.
 */
data class CycleDayMarkerFlags(
    val loggedPeriod: Boolean = false,
    val predictedPeriod: Boolean = false,
    val fertileWindow: Boolean = false,
    val estimatedOvulation: Boolean = false,
    val hasLogEntries: Boolean = false,
)

data class CycleDayMarkerInput(
    val date: LocalDate,
    val today: LocalDate,
    val periodSpans: List<PeriodSpanMarker>,
    val prediction: CyclePredictionResult,
    val datesWithLogEntries: Set<LocalDate> = emptySet(),
)

data class PeriodSpanMarker(
    val startDate: LocalDate,
    val endDate: LocalDate?,
)

object CycleDayMarkerBuilder {
    fun forDate(input: CycleDayMarkerInput): CycleDayMarkerFlags {
        val logged =
            input.periodSpans.any { span ->
                val inclusiveEnd =
                    when {
                        span.endDate != null -> span.endDate
                        else -> input.today
                    }
                !input.date.isBefore(span.startDate) && !input.date.isAfter(inclusiveEnd)
            }
        val predicted =
            input.prediction.nextPeriod?.let { range ->
                !input.date.isBefore(range.earliest) && !input.date.isAfter(range.latest)
            } == true
        val fertile =
            input.prediction.fertileWindow?.let { range ->
                !input.date.isBefore(range.earliest) && !input.date.isAfter(range.latest)
            } == true
        val ovulation =
            input.prediction.estimatedOvulation?.let { range ->
                !input.date.isBefore(range.earliest) && !input.date.isAfter(range.latest)
            } == true
        return CycleDayMarkerFlags(
            loggedPeriod = logged,
            predictedPeriod = predicted && !logged,
            fertileWindow = fertile,
            estimatedOvulation = ovulation,
            hasLogEntries = input.datesWithLogEntries.contains(input.date),
        )
    }

    @Suppress("LongParameterList")
    fun buildMap(
        rangeStart: LocalDate,
        rangeEnd: LocalDate,
        today: LocalDate,
        periodSpans: List<PeriodSpanMarker>,
        prediction: CyclePredictionResult,
        datesWithLogEntries: Set<LocalDate>,
    ): Map<LocalDate, CycleDayMarkerFlags> {
        val result = mutableMapOf<LocalDate, CycleDayMarkerFlags>()
        var day = rangeStart
        while (!day.isAfter(rangeEnd)) {
            result[day] =
                forDate(
                    CycleDayMarkerInput(
                        date = day,
                        today = today,
                        periodSpans = periodSpans,
                        prediction = prediction,
                        datesWithLogEntries = datesWithLogEntries,
                    ),
                )
            day = day.plusDays(1)
        }
        return result
    }
}
