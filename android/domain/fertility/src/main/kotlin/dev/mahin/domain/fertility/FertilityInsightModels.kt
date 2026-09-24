package dev.mahin.domain.fertility

import dev.mahin.domain.cycle.CyclePredictionResult
import dev.mahin.domain.cycle.DateRangeEstimate
import java.time.LocalDate

data class TtcSignalDay(
    val date: LocalDate,
    val bbtCelsius: Double?,
    val ovulationTestResult: String?,
    val cervicalMucus: String?,
    val intercourseLogged: Boolean,
    val pregnancyTestResult: String?,
)

data class FertilityInsightResult(
    val algorithmVersion: String,
    val cyclePrediction: CyclePredictionResult,
    val ovulationTestSurgeDates: List<LocalDate>,
    val bbtShiftSuggestedDate: LocalDate?,
    val fertileEggWhiteDates: List<LocalDate>,
    val signalAlignedWithEstimate: Boolean,
    val highlightedFertileWindow: DateRangeEstimate?,
)
