package dev.mahin.domain.pregnancy

import dev.mahin.core.model.PregnancyDatingSource
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Conventional LMP dating uses **280 days** (40 weeks) from LMP to estimated due date.
 * When a clinician/ultrasound EDD is supplied it becomes the effective EDD and gestational age
 * is derived from that EDD (280 − days remaining). Clinical review required before changing
 * these conventions — see ADR 0011.
 */
object PregnancyDatingEngineV1 {
    const val ALGORITHM_VERSION: String = "pregnancy-dating-v1"
    const val GESTATION_LENGTH_DAYS: Int = 280

    fun lmpBasedEdd(lmpDate: LocalDate): LocalDate = lmpDate.plusDays(GESTATION_LENGTH_DAYS.toLong())

    fun resolveDating(
        lmpDate: LocalDate,
        clinicalEddDate: LocalDate?,
    ): PregnancyDatingSnapshot {
        val lmpEdd = lmpBasedEdd(lmpDate)
        return if (clinicalEddDate != null) {
            PregnancyDatingSnapshot(
                lmpDate = lmpDate,
                lmpBasedEdd = lmpEdd,
                clinicalEddDate = clinicalEddDate,
                effectiveEddDate = clinicalEddDate,
                datingSource = PregnancyDatingSource.CLINICAL_OR_ULTRASOUND,
            )
        } else {
            PregnancyDatingSnapshot(
                lmpDate = lmpDate,
                lmpBasedEdd = lmpEdd,
                clinicalEddDate = null,
                effectiveEddDate = lmpEdd,
                datingSource = PregnancyDatingSource.LMP_PLUS_280_DAYS,
            )
        }
    }

    fun gestationalAge(
        dating: PregnancyDatingSnapshot,
        asOfDate: LocalDate,
    ): GestationalAge {
        val totalDays =
            when (dating.datingSource) {
                PregnancyDatingSource.LMP_PLUS_280_DAYS ->
                    ChronoUnit.DAYS
                        .between(dating.lmpDate, asOfDate)
                        .toInt()
                        .coerceAtLeast(0)
                PregnancyDatingSource.CLINICAL_OR_ULTRASOUND -> {
                    val daysRemaining = ChronoUnit.DAYS.between(asOfDate, dating.effectiveEddDate)
                    (GESTATION_LENGTH_DAYS - daysRemaining).toInt().coerceAtLeast(0)
                }
            }
        return GestationalAge(
            weeks = totalDays / 7,
            days = totalDays % 7,
            totalDays = totalDays,
        )
    }

    fun trimester(gestationalAge: GestationalAge): PregnancyTrimester =
        when {
            gestationalAge.weeks < 14 -> PregnancyTrimester.FIRST
            gestationalAge.weeks < 28 -> PregnancyTrimester.SECOND
            else -> PregnancyTrimester.THIRD
        }

    /**
     * Display week for week-by-week placeholders: 1-based, capped at 42 for UX stability.
     */
    fun displayWeekNumber(gestationalAge: GestationalAge): Int = (gestationalAge.weeks + 1).coerceIn(1, 42)

    fun daysUntilEdd(
        effectiveEddDate: LocalDate,
        asOfDate: LocalDate,
    ): Long = ChronoUnit.DAYS.between(asOfDate, effectiveEddDate)

    fun status(
        lmpDate: LocalDate,
        clinicalEddDate: LocalDate?,
        asOfDate: LocalDate,
    ): PregnancyStatusSnapshot {
        val dating = resolveDating(lmpDate, clinicalEddDate)
        val ga = gestationalAge(dating, asOfDate)
        return PregnancyStatusSnapshot(
            dating = dating,
            gestationalAge = ga,
            trimester = trimester(ga),
            daysUntilEdd = daysUntilEdd(dating.effectiveEddDate, asOfDate),
            displayWeekNumber = displayWeekNumber(ga),
        )
    }
}
