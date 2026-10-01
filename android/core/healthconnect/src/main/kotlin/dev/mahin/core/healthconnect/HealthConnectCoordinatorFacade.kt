package dev.mahin.core.healthconnect

import dev.mahin.domain.healthconnect.HealthConnectSyncResult

interface HealthConnectCoordinatorFacade {
    fun isLaunchFlagEnabled(): Boolean

    suspend fun refreshRevocationState(): HealthConnectSyncResult

    suspend fun setUserOptIn(optIn: Boolean)

    suspend fun importFromHealthConnect(): HealthConnectSyncResult

    suspend fun exportToHealthConnect(): HealthConnectSyncResult
}
