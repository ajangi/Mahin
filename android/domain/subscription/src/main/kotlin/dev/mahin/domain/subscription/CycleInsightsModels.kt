package dev.mahin.domain.subscription

import java.time.LocalDate

data class CycleInsightsInput(
    val periods: List<CycleInsightsPeriod>,
    val recentLogs: List<CycleInsightsDailyLog>,
    val includePremiumSections: Boolean,
)

data class CycleInsightsPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate?,
)

data class CycleInsightsDailyLog(
    val logDate: LocalDate,
    val symptomTags: List<String>,
    val moodTags: List<String>,
)

data class CycleInsightsResult(
    val algorithmVersion: String,
    val cycleLengthDays: IntRange?,
    val periodLengthDays: IntRange?,
    val completedCycleCount: Int,
    val recentSymptomTimeline: List<String>,
    val premium: CyclePremiumInsights?,
    val disclaimerFa: String,
)

data class CyclePremiumInsights(
    val cycleLengthTrendLabelFa: String,
    val symptomCoOccurrenceNotesFa: List<String>,
)
