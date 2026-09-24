package dev.mahin.domain.fertility

import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PregnancyTestResult
import dev.mahin.domain.cycle.CyclePredictionResult
import java.time.LocalDate

data class TtcSignalDay(
    val date: LocalDate,
    val bbtCelsius: Double?,
    val ovulationTestResult: OvulationTestResult?,
    val cervicalMucus: CervicalMucusType?,
    val intercourseLogged: Boolean,
    val pregnancyTestResult: PregnancyTestResult?,
)

data class FertilityInsightResult(
    val algorithmVersion: String,
    val cyclePrediction: CyclePredictionResult,
    /** Logged positive/peak OPK dates in the current cycle (facts only). */
    val ovulationTestSurgeDates: List<LocalDate>,
    /** First day of a sustained rise pattern in logged BBT for the current cycle (descriptive, not diagnosis). */
    val bbtShiftSuggestedDate: LocalDate?,
    /** Logged egg-white mucus dates in the current cycle. */
    val fertileEggWhiteDates: List<LocalDate>,
)
