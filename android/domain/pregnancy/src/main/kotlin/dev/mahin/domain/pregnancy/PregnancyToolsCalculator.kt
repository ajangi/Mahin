package dev.mahin.domain.pregnancy

import java.time.LocalDate

/**
 * Non-persisting pregnancy dating calculators for the tools hub. Uses [PregnancyDatingEngineV1] only.
 */
object PregnancyToolsCalculator {
    const val CONCEPTION_TO_TERM_DAYS: Int = 266

    data class ToolResult(
        val effectiveEdd: LocalDate,
        val gestationalAge: GestationalAge,
        val displayWeekNumber: Int,
    )

    fun fromLmp(
        lmpDate: LocalDate,
        asOfDate: LocalDate,
    ): ToolResult {
        val dating = PregnancyDatingEngineV1.resolveDating(lmpDate, clinicalEddDate = null)
        val ga = PregnancyDatingEngineV1.gestationalAge(dating, asOfDate)
        return ToolResult(
            effectiveEdd = dating.effectiveEddDate,
            gestationalAge = ga,
            displayWeekNumber = PregnancyDatingEngineV1.displayWeekNumber(ga),
        )
    }

    fun fromUltrasoundEdd(
        ultrasoundEdd: LocalDate,
        asOfDate: LocalDate,
    ): ToolResult {
        val estimatedLmp = ultrasoundEdd.minusDays(PregnancyDatingEngineV1.GESTATION_LENGTH_DAYS.toLong())
        val dating = PregnancyDatingEngineV1.resolveDating(estimatedLmp, ultrasoundEdd)
        val ga = PregnancyDatingEngineV1.gestationalAge(dating, asOfDate)
        return ToolResult(
            effectiveEdd = dating.effectiveEddDate,
            gestationalAge = ga,
            displayWeekNumber = PregnancyDatingEngineV1.displayWeekNumber(ga),
        )
    }

    fun fromConception(
        conceptionDate: LocalDate,
        asOfDate: LocalDate,
    ): ToolResult {
        val edd = conceptionDate.plusDays(CONCEPTION_TO_TERM_DAYS.toLong())
        val estimatedLmp = conceptionDate.minusDays(14)
        val dating = PregnancyDatingEngineV1.resolveDating(estimatedLmp, edd)
        val ga = PregnancyDatingEngineV1.gestationalAge(dating, asOfDate)
        return ToolResult(
            effectiveEdd = edd,
            gestationalAge = ga,
            displayWeekNumber = PregnancyDatingEngineV1.displayWeekNumber(ga),
        )
    }
}
