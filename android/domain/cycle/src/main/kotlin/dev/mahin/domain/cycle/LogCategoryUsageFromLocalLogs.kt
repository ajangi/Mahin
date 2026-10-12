package dev.mahin.domain.cycle

import dev.mahin.core.model.LogEditorCategory
import java.time.LocalDate

/**
 * Derives category usage counts from local logs over a fixed window (inclusive).
 * Used when opening the day editor so section order does not shift mid-session.
 */
object LogCategoryUsageFromLocalLogs {
    const val WINDOW_DAYS: Long = 90

    data class DailyLogUsageRow(
        val logDate: LocalDate,
        val moodTags: Set<String>,
        val symptomTags: Set<String>,
        val medicationTags: Set<String>,
        val localCustomSymptomTags: Set<String>,
        val painSeverity: Int?,
        val note: String?,
        val sleepLogged: Boolean,
        val exerciseLogged: Boolean,
        val waterGlasses: Int,
        val stressLevel: Int?,
    )

    data class TtcLogUsageRow(
        val logDate: LocalDate,
        val bbtLogged: Boolean,
        val cervicalMucusLogged: Boolean,
        val ovulationTestLogged: Boolean,
        val pregnancyTestLogged: Boolean,
        val intercourseLogged: Boolean,
    )

    data class PeriodDayUsageRow(
        val logDate: LocalDate,
        val flowLogged: Boolean,
    )

    @Suppress("LongParameterList", "CyclomaticComplexMethod")
    fun countUsage(
        referenceDate: LocalDate,
        dailyLogs: List<DailyLogUsageRow>,
        ttcLogs: List<TtcLogUsageRow>,
        periodDays: List<PeriodDayUsageRow>,
        pregnancySymptomDays: List<LocalDate> = emptyList(),
        pregnancyVitalDays: List<LocalDate> = emptyList(),
    ): Map<LogEditorCategory, Int> {
        val windowStart = referenceDate.minusDays(WINDOW_DAYS)
        val counts = mutableMapOf<LogEditorCategory, Int>()

        fun bump(category: LogEditorCategory) {
            counts[category] = (counts[category] ?: 0) + 1
        }
        dailyLogs.filter { !it.logDate.isBefore(windowStart) && !it.logDate.isAfter(referenceDate) }.forEach { row ->
            if (row.moodTags.isNotEmpty()) bump(LogEditorCategory.MOOD)
            if (row.symptomTags.isNotEmpty()) bump(LogEditorCategory.SYMPTOMS)
            if (row.localCustomSymptomTags.isNotEmpty()) bump(LogEditorCategory.SYMPTOMS)
            if (row.medicationTags.isNotEmpty()) bump(LogEditorCategory.MEDICATION)
            if (row.painSeverity != null) bump(LogEditorCategory.PAIN)
            if (!row.note.isNullOrBlank()) bump(LogEditorCategory.NOTES)
            if (row.sleepLogged) bump(LogEditorCategory.SLEEP)
            if (row.exerciseLogged) bump(LogEditorCategory.EXERCISE)
            if (row.waterGlasses > 0) bump(LogEditorCategory.WATER)
            if (row.stressLevel != null) bump(LogEditorCategory.STRESS)
        }
        ttcLogs.filter { !it.logDate.isBefore(windowStart) && !it.logDate.isAfter(referenceDate) }.forEach { row ->
            if (row.cervicalMucusLogged) bump(LogEditorCategory.DISCHARGE)
            if (row.ovulationTestLogged || row.pregnancyTestLogged) bump(LogEditorCategory.TTC_TESTS)
            if (row.intercourseLogged) bump(LogEditorCategory.SEXUAL_ACTIVITY)
            if (row.bbtLogged) bump(LogEditorCategory.TTC_TESTS)
        }
        periodDays.filter { !it.logDate.isBefore(windowStart) && !it.logDate.isAfter(referenceDate) }.forEach { row ->
            if (row.flowLogged) bump(LogEditorCategory.PERIOD_FLOW)
        }
        pregnancySymptomDays.filter { !it.isBefore(windowStart) && !it.isAfter(referenceDate) }.forEach {
            bump(LogEditorCategory.PREGNANCY_SYMPTOMS)
        }
        pregnancyVitalDays.filter { !it.isBefore(windowStart) && !it.isAfter(referenceDate) }.forEach {
            bump(LogEditorCategory.PREGNANCY_VITALS)
        }
        return counts
    }
}
