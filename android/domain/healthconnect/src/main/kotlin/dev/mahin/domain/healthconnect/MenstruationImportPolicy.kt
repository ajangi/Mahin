package dev.mahin.domain.healthconnect

import dev.mahin.core.model.PeriodFlowLevel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

object MenstruationImportPolicy {
    fun shouldSkipOwnAppRecord(
        dataOriginPackage: String,
        appPackageName: String,
    ): Boolean = dataOriginPackage == appPackageName

    /**
     * When Health Connect flow is a lossy image of the local level, keep local granularity.
     */
    fun shouldPreserveLocalFlowLevel(
        localLevel: PeriodFlowLevel,
        incomingFlow: HealthConnectMenstruationFlow,
    ): Boolean {
        val mapped = MenstruationFlowMapper.toHealthConnectFlow(localLevel) ?: return false
        return mapped == incomingFlow
    }

    fun localDateFromRecord(
        time: Instant,
        zoneOffset: ZoneOffset,
    ): LocalDate = time.atOffset(zoneOffset).toLocalDate()
}
