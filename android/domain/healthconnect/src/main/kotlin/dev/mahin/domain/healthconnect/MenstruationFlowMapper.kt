package dev.mahin.domain.healthconnect

import dev.mahin.core.model.PeriodFlowLevel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Product-approved Health Connect menstruation mapping (M10).
 * SDK record types: MenstruationFlowRecord, MenstruationPeriodRecord (see docs/health-connect/).
 */
enum class HealthConnectMenstruationFlow {
    UNKNOWN,
    LIGHT,
    MEDIUM,
    HEAVY,
}

data class HealthConnectMenstruationFlowDay(
    val localDate: LocalDate,
    val flow: HealthConnectMenstruationFlow,
    val sourceUpdatedAt: Instant,
)

data class HealthConnectMenstruationPeriodSpan(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val sourceUpdatedAt: Instant,
)

object MenstruationFlowMapper {
    fun toHealthConnectFlow(level: PeriodFlowLevel?): HealthConnectMenstruationFlow? =
        when (level) {
            null -> null
            PeriodFlowLevel.SPOTTING -> HealthConnectMenstruationFlow.LIGHT
            PeriodFlowLevel.LIGHT -> HealthConnectMenstruationFlow.LIGHT
            PeriodFlowLevel.MEDIUM -> HealthConnectMenstruationFlow.MEDIUM
            PeriodFlowLevel.HEAVY -> HealthConnectMenstruationFlow.HEAVY
            PeriodFlowLevel.VERY_HEAVY -> HealthConnectMenstruationFlow.HEAVY
        }

    fun toPeriodFlowLevel(flow: HealthConnectMenstruationFlow): PeriodFlowLevel? =
        when (flow) {
            HealthConnectMenstruationFlow.UNKNOWN -> null
            HealthConnectMenstruationFlow.LIGHT -> PeriodFlowLevel.LIGHT
            HealthConnectMenstruationFlow.MEDIUM -> PeriodFlowLevel.MEDIUM
            HealthConnectMenstruationFlow.HEAVY -> PeriodFlowLevel.HEAVY
        }

    fun localDateToInstantStartOfDay(
        date: LocalDate,
        zoneId: ZoneId,
    ): Instant = date.atStartOfDay(zoneId).toInstant()
}

object MenstruationImportMerger {
    /**
     * Keeps the row with the newest [sourceUpdatedAt] when both local and Health Connect have a day.
     */
    fun shouldReplaceLocal(
        localUpdatedAtEpochMs: Long,
        incoming: Instant,
    ): Boolean = incoming.toEpochMilli() > localUpdatedAtEpochMs
}
