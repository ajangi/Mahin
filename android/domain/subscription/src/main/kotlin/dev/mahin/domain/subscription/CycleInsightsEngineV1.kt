package dev.mahin.domain.subscription

import java.time.temporal.ChronoUnit

/**
 * Deterministic cycle observations for insights UI. Not medical advice or diagnosis.
 */
object CycleInsightsEngineV1 {
    private const val DISCLAIMER_FA =
        "این تحلیل‌ها بر اساس ثبت‌های شما هستند و جایگزین تشخیص پزشکی نیستند."

    fun build(input: CycleInsightsInput): CycleInsightsResult {
        val cycleLengths = deriveCycleLengths(input.periods)
        val periodLengths = derivePeriodLengths(input.periods)
        val timeline =
            input.recentLogs
                .sortedByDescending { it.logDate }
                .take(14)
                .flatMap { log -> log.symptomTags.map { tag -> "${log.logDate}: $tag" } }
                .take(10)

        val premium =
            if (input.includePremiumSections) {
                buildPremium(cycleLengths, input.recentLogs)
            } else {
                null
            }

        return CycleInsightsResult(
            algorithmVersion = SubscriptionDomainModule.INSIGHTS_ALGORITHM_VERSION,
            cycleLengthDays = intRangeOrNull(cycleLengths),
            periodLengthDays = intRangeOrNull(periodLengths),
            completedCycleCount = cycleLengths.size,
            recentSymptomTimeline = timeline,
            premium = premium,
            disclaimerFa = DISCLAIMER_FA,
        )
    }

    private fun buildPremium(
        cycleLengths: List<Int>,
        logs: List<CycleInsightsDailyLog>,
    ): CyclePremiumInsights {
        val trend =
            when {
                cycleLengths.size < 2 -> "برای روند چند‌سیکله هنوز داده کافی نیست."
                else -> {
                    val delta = cycleLengths.last() - cycleLengths.first()
                    when {
                        delta > 2 -> "طول سیکل‌های اخیر کمی افزایش یافته است (مشاهده، نه علت‌سنجی)."
                        delta < -2 -> "طول سیکل‌های اخیر کمی کاهش یافته است (مشاهده، نه علت‌سنجی)."
                        else -> "طول سیکل‌های اخیر تقریباً پایدار بوده است."
                    }
                }
            }
        val symptomCounts = mutableMapOf<String, Int>()
        logs.forEach { log ->
            log.symptomTags.forEach { tag -> symptomCounts[tag] = (symptomCounts[tag] ?: 0) + 1 }
        }
        val notes =
            symptomCounts.entries
                .sortedByDescending { it.value }
                .take(3)
                .map { entry ->
                    "«${entry.key}» در ${entry.value} روز از بازه اخیر ثبت شده است؛ " +
                        "ممکن است هم‌زمانی باشد، نه علت قطعی."
                }
        return CyclePremiumInsights(
            cycleLengthTrendLabelFa = trend,
            symptomCoOccurrenceNotesFa = notes,
        )
    }

    private fun deriveCycleLengths(periods: List<CycleInsightsPeriod>): List<Int> {
        val sorted = periods.sortedBy { it.startDate }
        if (sorted.size < 2) return emptyList()
        return sorted
            .zip(sorted.drop(1)) { current, next ->
                ChronoUnit.DAYS.between(current.startDate, next.startDate).toInt()
            }.filter { it in 15..60 }
    }

    private fun derivePeriodLengths(periods: List<CycleInsightsPeriod>): List<Int> =
        periods.mapNotNull { period ->
            val end = period.endDate ?: return@mapNotNull null
            val days = ChronoUnit.DAYS.between(period.startDate, end).toInt() + 1
            if (days in 1..14) days else null
        }

    private fun intRangeOrNull(values: List<Int>): IntRange? {
        if (values.isEmpty()) return null
        return values.min()..values.max()
    }
}
