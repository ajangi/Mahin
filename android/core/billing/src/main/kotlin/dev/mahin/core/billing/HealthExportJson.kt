package dev.mahin.core.billing

import dev.mahin.domain.subscription.LocalHealthExport
import kotlinx.serialization.Serializable

@Serializable
data class HealthExportJson(
    val formatVersion: String,
    val exportedAtEpochMs: Long,
    val periods: List<HealthExportPeriodJson>,
    val dailyLogs: List<HealthExportDailyLogJson>,
)

@Serializable
data class HealthExportPeriodJson(
    val startDate: String,
    val endDate: String?,
    val hasNote: Boolean,
)

@Serializable
data class HealthExportDailyLogJson(
    val logDate: String,
    val moodTags: List<String>,
    val symptomTags: List<String>,
    val painSeverity: Int?,
    val hasNote: Boolean,
)

fun LocalHealthExport.toJsonModel(): HealthExportJson =
    HealthExportJson(
        formatVersion = formatVersion,
        exportedAtEpochMs = exportedAtEpochMs,
        periods =
            periods.map {
                HealthExportPeriodJson(
                    startDate = it.startDate.toString(),
                    endDate = it.endDate?.toString(),
                    hasNote = it.hasNote,
                )
            },
        dailyLogs =
            dailyLogs.map {
                HealthExportDailyLogJson(
                    logDate = it.logDate.toString(),
                    moodTags = it.moodTags,
                    symptomTags = it.symptomTags,
                    painSeverity = it.painSeverity,
                    hasNote = it.hasNote,
                )
            },
    )
