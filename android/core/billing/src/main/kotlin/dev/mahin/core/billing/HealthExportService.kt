package dev.mahin.core.billing

import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.core.security.AppLockGateway
import dev.mahin.domain.subscription.EntitlementRules
import dev.mahin.domain.subscription.ExportDailyLogRow
import dev.mahin.domain.subscription.ExportPeriodRow
import dev.mahin.domain.subscription.LocalHealthExport
import dev.mahin.domain.subscription.LocalHealthExportBuilder
import dev.mahin.domain.subscription.PremiumFeature
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed interface HealthExportResult {
    data class Success(
        val json: String,
        val export: LocalHealthExport,
    ) : HealthExportResult

    data object Locked : HealthExportResult
}

@Singleton
class HealthExportService
    @Inject
    constructor(
        private val cycleTrackingRepository: CycleTrackingRepository,
        private val appLockGateway: AppLockGateway,
        private val entitlementRepository: EntitlementRepository,
    ) {
        private val json = Json { prettyPrint = true }

        suspend fun buildJsonExport(): HealthExportResult {
            if (appLockGateway.isLockEnabled) {
                return HealthExportResult.Locked
            }
            val entitlement = entitlementRepository.entitlement.first()
            val extended =
                EntitlementRules.canUse(
                    entitlement,
                    PremiumFeature.EXPORT_EXTENDED_LAYOUT,
                )
            val today = LocalDate.now()
            val periods = cycleTrackingRepository.observePeriods().first()
            val logs =
                cycleTrackingRepository
                    .observeDailyLogs(
                        start = today.minusYears(2),
                        end = today,
                    ).first()
            val export =
                LocalHealthExportBuilder.build(
                    periods = periods.map { it.toExportRow() },
                    dailyLogs = logs.map { it.toExportRow() },
                    exportedAtEpochMs = System.currentTimeMillis(),
                    extendedLayout = extended,
                )
            return HealthExportResult.Success(json.encodeToString(export.toJsonModel()), export)
        }

        private fun PeriodRecordEntity.toExportRow(): ExportPeriodRow =
            ExportPeriodRow(
                startDate = startDate,
                endDate = endDate,
                hasNote = !note.isNullOrBlank(),
            )

        private fun DailyLogEntity.toExportRow(): ExportDailyLogRow =
            ExportDailyLogRow(
                logDate = logDate,
                moodTags =
                    moodTags
                        .split(',')
                        .map { it.trim() }
                        .filter { it.isNotEmpty() },
                symptomTags =
                    symptomTags
                        .split(',')
                        .map { it.trim() }
                        .filter { it.isNotEmpty() },
                painSeverity = painSeverity,
                hasNote = !note.isNullOrBlank(),
            )
    }
