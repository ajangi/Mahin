package dev.mahin.android.cycle

import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.domain.cycle.CycleDayMarkerBuilder
import dev.mahin.domain.cycle.CycleDayMarkerFlags
import dev.mahin.domain.cycle.CycleDayMarkerInput
import dev.mahin.domain.cycle.CyclePredictionResult
import dev.mahin.domain.cycle.PeriodSpanMarker
import java.time.LocalDate

internal fun PeriodRecordEntity.toPeriodSpanMarker(): PeriodSpanMarker =
    PeriodSpanMarker(startDate = startDate, endDate = endDate)

internal fun CycleDayMarkerFlags.toDayMarkers(): DayMarkers =
    DayMarkers(
        loggedPeriod = loggedPeriod,
        predictedPeriod = predictedPeriod,
        fertileWindow = fertileWindow,
        estimatedOvulation = estimatedOvulation,
        hasLogEntries = hasLogEntries,
    )

internal object CycleDayMarkersMapper {
    fun forDate(
        date: LocalDate,
        today: LocalDate,
        periods: List<PeriodRecordEntity>,
        prediction: CyclePredictionResult,
        datesWithLogEntries: Set<LocalDate>,
    ): DayMarkers =
        CycleDayMarkerBuilder
            .forDate(
                CycleDayMarkerInput(
                    date = date,
                    today = today,
                    periodSpans = periods.map { it.toPeriodSpanMarker() },
                    prediction = prediction,
                    datesWithLogEntries = datesWithLogEntries,
                ),
            ).toDayMarkers()

    @Suppress("LongParameterList")
    fun buildMap(
        rangeStart: LocalDate,
        rangeEnd: LocalDate,
        today: LocalDate,
        periods: List<PeriodRecordEntity>,
        prediction: CyclePredictionResult,
        datesWithLogEntries: Set<LocalDate>,
    ): Map<LocalDate, DayMarkers> =
        CycleDayMarkerBuilder
            .buildMap(
                rangeStart = rangeStart,
                rangeEnd = rangeEnd,
                today = today,
                periodSpans = periods.map { it.toPeriodSpanMarker() },
                prediction = prediction,
                datesWithLogEntries = datesWithLogEntries,
            ).mapValues { (_, flags) -> flags.toDayMarkers() }
}
