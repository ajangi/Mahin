package dev.mahin.domain.subscription

import java.time.LocalDate

/**
 * Privacy-conscious export envelope. Free export includes typed summaries only;
 * extended layout is a Premium presentation concern on the client.
 */
data class LocalHealthExport(
    val formatVersion: String,
    val exportedAtEpochMs: Long,
    val periods: List<ExportPeriodRow>,
    val dailyLogs: List<ExportDailyLogRow>,
)

data class ExportPeriodRow(
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val hasNote: Boolean,
)

data class ExportDailyLogRow(
    val logDate: LocalDate,
    val moodTags: List<String>,
    val symptomTags: List<String>,
    val painSeverity: Int?,
    val hasNote: Boolean,
)

object LocalHealthExportBuilder {
    fun build(
        periods: List<ExportPeriodRow>,
        dailyLogs: List<ExportDailyLogRow>,
        exportedAtEpochMs: Long,
        extendedLayout: Boolean,
    ): LocalHealthExport {
        val sortedLogs =
            if (extendedLayout) {
                dailyLogs.sortedByDescending { it.logDate }
            } else {
                dailyLogs.sortedByDescending { it.logDate }.take(90)
            }
        return LocalHealthExport(
            formatVersion = SubscriptionDomainModule.EXPORT_FORMAT_VERSION,
            exportedAtEpochMs = exportedAtEpochMs,
            periods = periods.sortedByDescending { it.startDate },
            dailyLogs = sortedLogs,
        )
    }
}
