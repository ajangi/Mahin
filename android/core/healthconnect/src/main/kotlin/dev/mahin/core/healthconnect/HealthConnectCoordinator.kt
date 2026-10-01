package dev.mahin.core.healthconnect

import dev.mahin.core.config.FeatureFlagGateway
import dev.mahin.core.config.MahinFeatureFlags
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.datastore.HealthConnectPreferencesRepository
import dev.mahin.core.security.AppLockGateway
import dev.mahin.domain.healthconnect.HealthConnectMenstruationFlowDay
import dev.mahin.domain.healthconnect.MenstruationFlowMapper
import dev.mahin.domain.healthconnect.MenstruationImportMerger
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class HealthConnectCoordinator
    @Inject
    constructor(
        private val featureFlagGateway: FeatureFlagGateway,
        private val preferencesRepository: HealthConnectPreferencesRepository,
        private val appLockGateway: AppLockGateway,
        private val cycleTrackingRepository: CycleTrackingRepository,
        private val clientGateway: HealthConnectClientGateway,
    ) {
        fun isLaunchFlagEnabled(): Boolean = featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_CONNECT)

        suspend fun refreshRevocationState(): HealthConnectSyncResult {
            val prefs = preferencesRepository.snapshot.first()
            val granted = clientGateway.grantedPermissionStrings()
            val allGranted = HealthConnectPermissionPolicy.hasAllGranted(granted)
            val evaluated =
                HealthConnectRevocationEvaluator.evaluate(
                    launchFlagEnabled = isLaunchFlagEnabled(),
                    userOptIn = prefs.userOptIn,
                    permissionsPreviouslyGranted = prefs.permissionsPreviouslyGranted,
                    allPermissionsGranted = allGranted,
                )
            if (evaluated == HealthConnectSyncResult.PermissionsRevoked) {
                preferencesRepository.clearIntegrationState()
                return evaluated
            }
            if (evaluated != null) {
                return evaluated
            }
            preferencesRepository.setPermissionsPreviouslyGranted(true)
            return HealthConnectSyncResult.Success()
        }

        suspend fun setUserOptIn(optIn: Boolean) {
            if (!isLaunchFlagEnabled()) return
            preferencesRepository.setUserOptIn(optIn)
            if (!optIn) {
                preferencesRepository.clearIntegrationState()
            }
        }

        suspend fun importFromHealthConnect(): HealthConnectSyncResult {
            syncPrecondition()?.let { return it }
            return runCatching {
                val incoming = clientGateway.importMenstruationFlowDays()
                var imported = 0
                incoming.forEach { day ->
                    if (mergeDay(day)) imported++
                }
                HealthConnectSyncResult.Success(importedDays = imported)
            }.getOrElse { error ->
                HealthConnectSyncResult.Failure(error.javaClass.simpleName)
            }
        }

        suspend fun exportToHealthConnect(): HealthConnectSyncResult {
            syncPrecondition()?.let { return it }
            return runCatching {
                val today = LocalDate.now()
                val start = today.minusYears(2)
                val periodDays =
                    cycleTrackingRepository.observePeriodDays(start, today).first()
                val exportDays =
                    periodDays.mapNotNull { entity ->
                        val flow =
                            MenstruationFlowMapper.toHealthConnectFlow(entity.flowLevel)
                                ?: return@mapNotNull null
                        HealthConnectMenstruationFlowDay(
                            localDate = entity.logDate,
                            flow = flow,
                            sourceUpdatedAt =
                                java.time.Instant.ofEpochMilli(entity.updatedAtEpochMs),
                        )
                    }
                val exported = clientGateway.exportMenstruationFlowDays(exportDays)
                HealthConnectSyncResult.Success(exportedDays = exported)
            }.getOrElse { error ->
                HealthConnectSyncResult.Failure(error.javaClass.simpleName)
            }
        }

        private suspend fun mergeDay(day: HealthConnectMenstruationFlowDay): Boolean {
            val level = MenstruationFlowMapper.toPeriodFlowLevel(day.flow) ?: return false
            val existing = cycleTrackingRepository.getPeriodDayForDate(day.localDate)
            if (existing != null &&
                !MenstruationImportMerger.shouldReplaceLocal(existing.updatedAtEpochMs, day.sourceUpdatedAt)
            ) {
                return false
            }
            cycleTrackingRepository.upsertPeriodDay(
                date = day.localDate,
                flowLevel = level,
                hasClots = existing?.hasClots ?: false,
            )
            return true
        }

        private suspend fun syncPrecondition(): HealthConnectSyncResult? {
            val blockReason =
                when {
                    !isLaunchFlagEnabled() -> HealthConnectSyncResult.FeatureDisabled
                    appLockGateway.requiresUnlockForSensitiveAction() -> HealthConnectSyncResult.Locked
                    !preferencesRepository.snapshot.first().userOptIn -> HealthConnectSyncResult.NotOptedIn
                    !HealthConnectPermissionPolicy.hasAllGranted(clientGateway.grantedPermissionStrings()) ->
                        HealthConnectSyncResult.PermissionsMissing
                    else -> null
                }
            return blockReason
        }
    }
