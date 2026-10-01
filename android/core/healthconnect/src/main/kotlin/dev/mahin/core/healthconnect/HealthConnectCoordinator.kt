package dev.mahin.core.healthconnect

import dev.mahin.core.config.FeatureFlagGateway
import dev.mahin.core.config.MahinFeatureFlags
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.datastore.HealthConnectPeriodDayTombstoneRepository
import dev.mahin.core.datastore.HealthConnectPreferencesRepository
import dev.mahin.core.security.AppLockGateway
import dev.mahin.domain.healthconnect.HealthConnectAppLock
import dev.mahin.domain.healthconnect.HealthConnectRemoteClient
import dev.mahin.domain.healthconnect.HealthConnectSyncEngine
import dev.mahin.domain.healthconnect.HealthConnectSyncResult
import dev.mahin.domain.healthconnect.HealthConnectUserPreferencesSnapshot
import dev.mahin.domain.healthconnect.HealthConnectUserPreferencesStore
import dev.mahin.domain.healthconnect.PeriodDayRow
import dev.mahin.domain.healthconnect.PeriodDayStore
import dev.mahin.domain.healthconnect.PeriodDayTombstoneStore
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
@Suppress("LongParameterList")
class HealthConnectCoordinator
    @Inject
    constructor(
        featureFlagGateway: FeatureFlagGateway,
        preferencesRepository: HealthConnectPreferencesRepository,
        tombstoneRepository: HealthConnectPeriodDayTombstoneRepository,
        appLockGateway: AppLockGateway,
        cycleTrackingRepository: CycleTrackingRepository,
        remoteClient: HealthConnectRemoteClient,
        @dagger.hilt.android.qualifiers.ApplicationContext appContext: android.content.Context,
    ) : HealthConnectCoordinatorFacade {
        private val engine: HealthConnectSyncEngine =
            HealthConnectSyncEngine(
                isLaunchFlagEnabled = { featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_CONNECT) },
                appPackageName = appContext.packageName,
                remoteClient = remoteClient,
                preferences = PreferencesAdapter(preferencesRepository),
                appLock = AppLockAdapter(appLockGateway),
                periodDays = PeriodDayStoreAdapter(cycleTrackingRepository),
                tombstones = TombstoneStoreAdapter(tombstoneRepository),
                requiredPermissions = HealthConnectPermissionPolicy.requiredPermissions,
            )

        override fun isLaunchFlagEnabled(): Boolean = engine.launchFlagEnabled()

        override suspend fun refreshRevocationState(): HealthConnectSyncResult = engine.refreshRevocationState()

        override suspend fun setUserOptIn(optIn: Boolean) = engine.setUserOptIn(optIn)

        override suspend fun importFromHealthConnect(): HealthConnectSyncResult = engine.importFromHealthConnect()

        override suspend fun exportToHealthConnect(): HealthConnectSyncResult = engine.exportToHealthConnect()
    }

private class PreferencesAdapter(
    private val repository: HealthConnectPreferencesRepository,
) : HealthConnectUserPreferencesStore {
    override suspend fun snapshot(): HealthConnectUserPreferencesSnapshot {
        val prefs = repository.snapshot.first()
        return HealthConnectUserPreferencesSnapshot(
            userOptIn = prefs.userOptIn,
            permissionsPreviouslyGranted = prefs.permissionsPreviouslyGranted,
        )
    }

    override suspend fun setUserOptIn(enabled: Boolean) = repository.setUserOptIn(enabled)

    override suspend fun setPermissionsPreviouslyGranted(granted: Boolean) =
        repository.setPermissionsPreviouslyGranted(granted)

    override suspend fun clearIntegrationState() = repository.clearIntegrationState()
}

private class AppLockAdapter(
    private val gateway: AppLockGateway,
) : HealthConnectAppLock {
    override fun requiresUnlockForSensitiveAction(): Boolean = gateway.requiresUnlockForSensitiveAction()
}

private class PeriodDayStoreAdapter(
    private val repository: CycleTrackingRepository,
) : PeriodDayStore {
    override suspend fun getPeriodDay(date: LocalDate): PeriodDayRow? =
        repository.getPeriodDayForDate(date)?.let {
            PeriodDayRow(
                logDate = it.logDate,
                flowLevel = it.flowLevel,
                hasClots = it.hasClots,
                updatedAtEpochMs = it.updatedAtEpochMs,
            )
        }

    override suspend fun upsertPeriodDay(
        date: LocalDate,
        flowLevel: dev.mahin.core.model.PeriodFlowLevel?,
        hasClots: Boolean,
        updatedAtEpochMs: Long,
    ) {
        repository.upsertPeriodDay(
            date = date,
            flowLevel = flowLevel,
            hasClots = hasClots,
            updatedAtEpochMs = updatedAtEpochMs,
        )
    }

    override suspend fun periodDaysInRange(
        start: LocalDate,
        end: LocalDate,
    ): List<PeriodDayRow> =
        repository.getPeriodDaysInRange(start, end).map {
            PeriodDayRow(
                logDate = it.logDate,
                flowLevel = it.flowLevel,
                hasClots = it.hasClots,
                updatedAtEpochMs = it.updatedAtEpochMs,
            )
        }
}

private class TombstoneStoreAdapter(
    private val repository: HealthConnectPeriodDayTombstoneRepository,
) : PeriodDayTombstoneStore {
    override suspend fun isUserDeleted(date: LocalDate): Boolean = repository.isUserDeleted(date)

    override suspend fun userDeletedDatesInRange(
        start: LocalDate,
        end: LocalDate,
    ): Set<LocalDate> = repository.userDeletedDatesInRange(start, end)

    override suspend fun clearAll() = repository.clearAll()
}
