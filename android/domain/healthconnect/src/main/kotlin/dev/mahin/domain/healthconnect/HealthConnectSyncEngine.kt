package dev.mahin.domain.healthconnect

import dev.mahin.core.model.PeriodFlowLevel
import java.time.LocalDate

data class PeriodDayRow(
    val logDate: LocalDate,
    val flowLevel: PeriodFlowLevel?,
    val hasClots: Boolean,
    val updatedAtEpochMs: Long,
)

sealed interface HealthConnectSyncResult {
    data object FeatureDisabled : HealthConnectSyncResult

    data object Locked : HealthConnectSyncResult

    data object NotOptedIn : HealthConnectSyncResult

    data object PermissionsMissing : HealthConnectSyncResult

    data object PermissionsRevoked : HealthConnectSyncResult

    data class Success(
        val importedDays: Int = 0,
        val exportedDays: Int = 0,
        val deletedRemoteDays: Int = 0,
    ) : HealthConnectSyncResult

    data class Failure(
        val errorClass: String,
    ) : HealthConnectSyncResult
}

sealed interface HealthConnectClientResult<out T> {
    data class Ok<T>(
        val value: T,
    ) : HealthConnectClientResult<T>

    data object NotReady : HealthConnectClientResult<Nothing>

    data object PermissionsMissing : HealthConnectClientResult<Nothing>

    data class Error(
        val errorClass: String,
    ) : HealthConnectClientResult<Nothing>
}

interface HealthConnectRemoteClient {
    suspend fun availability(): HealthConnectAvailability

    suspend fun grantedPermissionStrings(): HealthConnectClientResult<Set<String>>

    suspend fun readMenstruationFlowDays(): HealthConnectClientResult<List<HealthConnectMenstruationFlowDay>>

    suspend fun upsertMenstruationFlowExports(
        exports: List<MenstruationFlowExportWrite>,
    ): HealthConnectClientResult<Int>

    suspend fun deleteMenstruationByClientRecordIds(clientRecordIds: List<String>): HealthConnectClientResult<Int>
}

interface PeriodDayTombstoneStore {
    suspend fun isUserDeleted(date: LocalDate): Boolean

    suspend fun userDeletedDatesInRange(
        start: LocalDate,
        end: LocalDate,
    ): Set<LocalDate>
}

interface PeriodDayStore {
    suspend fun getPeriodDay(date: LocalDate): PeriodDayRow?

    suspend fun upsertPeriodDay(
        date: LocalDate,
        flowLevel: PeriodFlowLevel?,
        hasClots: Boolean,
        updatedAtEpochMs: Long,
    )

    suspend fun periodDaysInRange(
        start: LocalDate,
        end: LocalDate,
    ): List<PeriodDayRow>
}

data class HealthConnectUserPreferencesSnapshot(
    val userOptIn: Boolean = false,
    val permissionsPreviouslyGranted: Boolean = false,
)

interface HealthConnectUserPreferencesStore {
    suspend fun snapshot(): HealthConnectUserPreferencesSnapshot

    suspend fun setUserOptIn(enabled: Boolean)

    suspend fun setPermissionsPreviouslyGranted(granted: Boolean)

    suspend fun clearIntegrationState()
}

interface HealthConnectAppLock {
    fun requiresUnlockForSensitiveAction(): Boolean
}

@Suppress("LongParameterList")
class HealthConnectSyncEngine(
    private val isLaunchFlagEnabled: () -> Boolean,
    private val appPackageName: String,
    private val remoteClient: HealthConnectRemoteClient,
    private val preferences: HealthConnectUserPreferencesStore,
    private val appLock: HealthConnectAppLock,
    private val periodDays: PeriodDayStore,
    private val tombstones: PeriodDayTombstoneStore,
    private val requiredPermissions: Set<String>,
    private val clock: () -> LocalDate = { LocalDate.now() },
) {
    fun launchFlagEnabled(): Boolean = isLaunchFlagEnabled()

    @Suppress("ReturnCount")
    suspend fun refreshRevocationState(): HealthConnectSyncResult {
        if (!isLaunchFlagEnabled()) return HealthConnectSyncResult.FeatureDisabled
        val prefs = preferences.snapshot()
        val granted = loadGrantedPermissions() ?: return HealthConnectSyncResult.PermissionsMissing
        val evaluated =
            HealthConnectRevocationEvaluator.evaluate(
                launchFlagEnabled = true,
                userOptIn = prefs.userOptIn,
                permissionsPreviouslyGranted = prefs.permissionsPreviouslyGranted,
                allPermissionsGranted = hasAllPermissions(granted),
            )
        if (evaluated == HealthConnectSyncResult.PermissionsRevoked) {
            preferences.clearIntegrationState()
            return evaluated
        }
        if (evaluated != null) return evaluated
        preferences.setPermissionsPreviouslyGranted(true)
        return HealthConnectSyncResult.Success()
    }

    suspend fun setUserOptIn(optIn: Boolean) {
        if (!isLaunchFlagEnabled()) return
        preferences.setUserOptIn(optIn)
        if (!optIn) preferences.clearIntegrationState()
    }

    suspend fun importFromHealthConnect(): HealthConnectSyncResult {
        syncPrecondition()?.let { return it }
        return when (val incoming = remoteClient.readMenstruationFlowDays()) {
            is HealthConnectClientResult.NotReady ->
                HealthConnectSyncResult.Failure("NotReady")
            is HealthConnectClientResult.PermissionsMissing -> HealthConnectSyncResult.PermissionsMissing
            is HealthConnectClientResult.Error -> HealthConnectSyncResult.Failure(incoming.errorClass)
            is HealthConnectClientResult.Ok -> {
                var imported = 0
                incoming.value.forEach { day ->
                    if (mergeImportedDay(day)) imported++
                }
                HealthConnectSyncResult.Success(importedDays = imported)
            }
        }
    }

    @Suppress("ReturnCount")
    suspend fun exportToHealthConnect(): HealthConnectSyncResult {
        syncPrecondition()?.let { return it }
        val today = clock()
        val start = today.minusYears(2)
        val tombstoned = tombstones.userDeletedDatesInRange(start, today)
        val deleteIds = tombstoned.map { MenstruationExportIds.clientRecordId(it) }
        val deletedRemote =
            if (deleteIds.isEmpty()) {
                0
            } else {
                when (val deleted = remoteClient.deleteMenstruationByClientRecordIds(deleteIds)) {
                    is HealthConnectClientResult.Ok -> deleted.value
                    is HealthConnectClientResult.NotReady -> return HealthConnectSyncResult.Failure("NotReady")
                    is HealthConnectClientResult.PermissionsMissing -> return HealthConnectSyncResult.PermissionsMissing
                    is HealthConnectClientResult.Error -> return HealthConnectSyncResult.Failure(deleted.errorClass)
                }
            }
        val exportWrites =
            periodDays.periodDaysInRange(start, today).mapNotNull { row ->
                if (tombstones.isUserDeleted(row.logDate)) return@mapNotNull null
                val flow = MenstruationFlowMapper.toHealthConnectFlow(row.flowLevel) ?: return@mapNotNull null
                MenstruationFlowExportWrite(
                    localDate = row.logDate,
                    flow = flow,
                    updatedAtEpochMs = row.updatedAtEpochMs,
                )
            }
        val exported =
            when (val upsert = remoteClient.upsertMenstruationFlowExports(exportWrites)) {
                is HealthConnectClientResult.Ok -> upsert.value
                is HealthConnectClientResult.NotReady -> return HealthConnectSyncResult.Failure("NotReady")
                is HealthConnectClientResult.PermissionsMissing -> return HealthConnectSyncResult.PermissionsMissing
                is HealthConnectClientResult.Error -> return HealthConnectSyncResult.Failure(upsert.errorClass)
            }
        return HealthConnectSyncResult.Success(exportedDays = exported, deletedRemoteDays = deletedRemote)
    }

    @Suppress("ReturnCount")
    private suspend fun mergeImportedDay(day: HealthConnectMenstruationFlowDay): Boolean {
        if (MenstruationImportPolicy.shouldSkipOwnAppRecord(day.dataOriginPackage, appPackageName)) {
            return false
        }
        if (tombstones.isUserDeleted(day.localDate)) return false
        val incomingLevel = MenstruationFlowMapper.toPeriodFlowLevel(day.flow) ?: return false
        val existing = periodDays.getPeriodDay(day.localDate)
        if (existing?.flowLevel != null &&
            MenstruationImportPolicy.shouldPreserveLocalFlowLevel(existing.flowLevel, day.flow)
        ) {
            return false
        }
        if (existing != null &&
            !MenstruationImportMerger.shouldReplaceLocal(existing.updatedAtEpochMs, day.sourceUpdatedAt)
        ) {
            return false
        }
        periodDays.upsertPeriodDay(
            date = day.localDate,
            flowLevel = incomingLevel,
            hasClots = existing?.hasClots ?: false,
            updatedAtEpochMs = day.sourceUpdatedAt.toEpochMilli(),
        )
        return true
    }

    private suspend fun syncPrecondition(): HealthConnectSyncResult? =
        when {
            !isLaunchFlagEnabled() -> HealthConnectSyncResult.FeatureDisabled
            appLock.requiresUnlockForSensitiveAction() -> HealthConnectSyncResult.Locked
            !preferences.snapshot().userOptIn -> HealthConnectSyncResult.NotOptedIn
            !hasAllPermissions(loadGrantedPermissions() ?: emptySet()) ->
                HealthConnectSyncResult.PermissionsMissing
            else -> null
        }

    private suspend fun loadGrantedPermissions(): Set<String>? {
        if (remoteClient.availability() != HealthConnectAvailability.READY) return emptySet()
        return when (val result = remoteClient.grantedPermissionStrings()) {
            is HealthConnectClientResult.Ok -> result.value
            is HealthConnectClientResult.PermissionsMissing -> null
            is HealthConnectClientResult.NotReady -> emptySet()
            is HealthConnectClientResult.Error -> emptySet()
        }
    }

    private fun hasAllPermissions(granted: Set<String>): Boolean = requiredPermissions.all { it in granted }
}
