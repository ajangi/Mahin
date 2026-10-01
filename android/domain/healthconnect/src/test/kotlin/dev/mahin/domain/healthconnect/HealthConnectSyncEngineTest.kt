package dev.mahin.domain.healthconnect

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.PeriodFlowLevel
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Test

private typealias MenstruationFlowDaysResult =
    HealthConnectClientResult<List<HealthConnectMenstruationFlowDay>>

class HealthConnectSyncEngineTest {
    private val appPackage = "dev.mahin.android"
    private val permissions = setOf("read", "write")
    private val today = LocalDate.of(2024, 6, 10)

    @Test
    fun flagOffMakesZeroRemoteCalls() =
        runTest {
            val remote = RecordingRemoteClient()
            val engine = engine(remote = remote, flagEnabled = false)
            assertThat(engine.importFromHealthConnect()).isEqualTo(HealthConnectSyncResult.FeatureDisabled)
            assertThat(engine.exportToHealthConnect()).isEqualTo(HealthConnectSyncResult.FeatureDisabled)
            assertThat(remote.callCount).isEqualTo(0)
        }

    @Test
    fun lockedBlocksSync() =
        runTest {
            val engine = engine(remote = RecordingRemoteClient(), locked = true)
            assertThat(engine.importFromHealthConnect()).isEqualTo(HealthConnectSyncResult.Locked)
        }

    @Test
    fun notOptedInBlocksSync() =
        runTest {
            val engine = engine(remote = RecordingRemoteClient(), userOptIn = false)
            assertThat(engine.exportToHealthConnect()).isEqualTo(HealthConnectSyncResult.NotOptedIn)
        }

    @Test
    fun permissionsMissingWhenNotGranted() =
        runTest {
            val remote = RecordingRemoteClient(granted = emptySet())
            val engine = engine(remote = remote, userOptIn = true)
            assertThat(engine.importFromHealthConnect()).isEqualTo(HealthConnectSyncResult.PermissionsMissing)
        }

    @Test
    fun revocationClearsPreferences() =
        runTest {
            val prefs = FakePreferences(userOptIn = true, permissionsPreviouslyGranted = true)
            val remote = RecordingRemoteClient(granted = emptySet())
            val engine = engine(remote = remote, preferences = prefs)
            assertThat(engine.refreshRevocationState()).isEqualTo(HealthConnectSyncResult.PermissionsRevoked)
            assertThat(prefs.cleared).isTrue()
        }

    @Test
    fun importSkipsOwnAppRecords() =
        runTest {
            val periodDays = FakePeriodDays()
            val remote =
                RecordingRemoteClient(
                    readResult =
                        listOf(
                            flowDay(
                                LocalDate.of(2024, 6, 1),
                                HealthConnectMenstruationFlow.MEDIUM,
                                dataOrigin = appPackage,
                            ),
                        ),
                )
            val engine = engine(remote = remote, periodDays = periodDays)
            val result = engine.importFromHealthConnect() as HealthConnectSyncResult.Success
            assertThat(result.importedDays).isEqualTo(0)
        }

    @Test
    fun importPreservesLossyLocalLevel() =
        runTest {
            val date = LocalDate.of(2024, 6, 2)
            val periodDays =
                FakePeriodDays(
                    initial =
                        mapOf(
                            date to
                                PeriodDayRow(
                                    logDate = date,
                                    flowLevel = PeriodFlowLevel.SPOTTING,
                                    hasClots = false,
                                    updatedAtEpochMs = 1L,
                                ),
                        ),
                )
            val remote =
                RecordingRemoteClient(
                    readResult =
                        listOf(
                            flowDay(
                                date,
                                HealthConnectMenstruationFlow.LIGHT,
                                updatedAt = Instant.ofEpochMilli(9_000),
                                dataOrigin = "other.app",
                            ),
                        ),
                )
            val engine = engine(remote = remote, periodDays = periodDays)
            engine.importFromHealthConnect()
            assertThat(periodDays.get(date)?.flowLevel).isEqualTo(PeriodFlowLevel.SPOTTING)
        }

    @Test
    fun importNewerWins() =
        runTest {
            val date = LocalDate.of(2024, 6, 3)
            val periodDays =
                FakePeriodDays(
                    initial =
                        mapOf(
                            date to
                                PeriodDayRow(
                                    logDate = date,
                                    flowLevel = PeriodFlowLevel.LIGHT,
                                    hasClots = false,
                                    updatedAtEpochMs = 100L,
                                ),
                        ),
                )
            val remote =
                RecordingRemoteClient(
                    readResult =
                        listOf(
                            flowDay(
                                date,
                                HealthConnectMenstruationFlow.HEAVY,
                                updatedAt = Instant.ofEpochMilli(200L),
                                dataOrigin = "other.app",
                            ),
                        ),
                )
            val engine = engine(remote = remote, periodDays = periodDays)
            engine.importFromHealthConnect()
            assertThat(periodDays.get(date)?.flowLevel).isEqualTo(PeriodFlowLevel.HEAVY)
        }

    @Test
    fun importRespectsTombstone() =
        runTest {
            val date = LocalDate.of(2024, 6, 4)
            val tombstones = FakeTombstones(deleted = setOf(date))
            val periodDays = FakePeriodDays()
            val remote =
                RecordingRemoteClient(
                    readResult =
                        listOf(
                            flowDay(
                                date,
                                HealthConnectMenstruationFlow.MEDIUM,
                                dataOrigin = "other.app",
                            ),
                        ),
                )
            val engine = engine(remote = remote, periodDays = periodDays, tombstones = tombstones)
            val result = engine.importFromHealthConnect() as HealthConnectSyncResult.Success
            assertThat(result.importedDays).isEqualTo(0)
            assertThat(periodDays.get(date)).isNull()
        }

    @Test
    fun importAggregatesMultipleRecords() =
        runTest {
            val remote =
                RecordingRemoteClient(
                    readResult =
                        listOf(
                            flowDay(LocalDate.of(2024, 6, 7), HealthConnectMenstruationFlow.LIGHT),
                            flowDay(LocalDate.of(2024, 6, 8), HealthConnectMenstruationFlow.MEDIUM),
                        ),
                )
            val periodDays = FakePeriodDays()
            val engine = engine(remote = remote, periodDays = periodDays)
            val result = engine.importFromHealthConnect() as HealthConnectSyncResult.Success
            assertThat(result.importedDays).isEqualTo(2)
        }

    @Test
    fun exportUsesStableClientRecordIdAndVersion() =
        runTest {
            val date = LocalDate.of(2024, 6, 5)
            val periodDays =
                FakePeriodDays(
                    initial =
                        mapOf(
                            date to
                                PeriodDayRow(
                                    logDate = date,
                                    flowLevel = PeriodFlowLevel.MEDIUM,
                                    hasClots = false,
                                    updatedAtEpochMs = 42L,
                                ),
                        ),
                )
            val remote = RecordingRemoteClient()
            val engine = engine(remote = remote, periodDays = periodDays, clock = { today })
            engine.exportToHealthConnect()
            assertThat(remote.lastExport).hasSize(1)
            assertThat(remote.lastExport.first().updatedAtEpochMs).isEqualTo(42L)
            assertThat(remote.lastExport.first().localDate).isEqualTo(date)
        }

    @Test
    fun exportDeletesTombstonedClientRecordIds() =
        runTest {
            val deleted = LocalDate.of(2024, 6, 6)
            val tombstones = FakeTombstones(deleted = setOf(deleted))
            val remote = RecordingRemoteClient()
            val engine = engine(remote = remote, tombstones = tombstones, clock = { today })
            val result = engine.exportToHealthConnect() as HealthConnectSyncResult.Success
            assertThat(result.deletedRemoteDays).isEqualTo(1)
            assertThat(remote.lastDeletedIds).contains(MenstruationExportIds.clientRecordId(deleted))
        }

    @Test
    fun throwingClientReturnsFailureWithoutCrashing() =
        runTest {
            val remote = RecordingRemoteClient(throwOnRead = true)
            val engine = engine(remote = remote)
            val result = engine.importFromHealthConnect()
            assertThat(result).isInstanceOf(HealthConnectSyncResult.Failure::class.java)
        }

    private fun flowDay(
        date: LocalDate,
        flow: HealthConnectMenstruationFlow,
        updatedAt: Instant = Instant.ofEpochMilli(5_000),
        dataOrigin: String = "other.app",
    ): HealthConnectMenstruationFlowDay =
        HealthConnectMenstruationFlowDay(
            localDate = date,
            flow = flow,
            sourceUpdatedAt = updatedAt,
            dataOriginPackage = dataOrigin,
        )

    @Suppress("LongParameterList")
    private fun engine(
        remote: HealthConnectRemoteClient,
        flagEnabled: Boolean = true,
        userOptIn: Boolean = true,
        locked: Boolean = false,
        preferences: FakePreferences = FakePreferences(userOptIn = userOptIn, permissionsPreviouslyGranted = true),
        periodDays: FakePeriodDays = FakePeriodDays(),
        tombstones: FakeTombstones = FakeTombstones(),
        clock: () -> LocalDate = { today },
    ): HealthConnectSyncEngine =
        HealthConnectSyncEngine(
            isLaunchFlagEnabled = { flagEnabled },
            appPackageName = appPackage,
            remoteClient = remote,
            preferences = preferences,
            appLock = FakeAppLock(locked),
            periodDays = periodDays,
            tombstones = tombstones,
            requiredPermissions = permissions,
            clock = clock,
        )

    private class FakeAppLock(
        private val locked: Boolean,
    ) : HealthConnectAppLock {
        override fun requiresUnlockForSensitiveAction(): Boolean = locked
    }

    private class FakePreferences(
        userOptIn: Boolean,
        permissionsPreviouslyGranted: Boolean,
    ) : HealthConnectUserPreferencesStore {
        private var prefs =
            HealthConnectUserPreferencesSnapshot(
                userOptIn = userOptIn,
                permissionsPreviouslyGranted = permissionsPreviouslyGranted,
            )
        var cleared = false

        override suspend fun snapshot(): HealthConnectUserPreferencesSnapshot = prefs

        override suspend fun setUserOptIn(enabled: Boolean) {
            prefs = prefs.copy(userOptIn = enabled)
        }

        override suspend fun setPermissionsPreviouslyGranted(granted: Boolean) {
            prefs = prefs.copy(permissionsPreviouslyGranted = granted)
        }

        override suspend fun clearIntegrationState() {
            cleared = true
            prefs = HealthConnectUserPreferencesSnapshot()
        }
    }

    private class FakePeriodDays(
        initial: Map<LocalDate, PeriodDayRow> = emptyMap(),
    ) : PeriodDayStore {
        private val rows = initial.toMutableMap()

        fun get(date: LocalDate): PeriodDayRow? = rows[date]

        override suspend fun getPeriodDay(date: LocalDate): PeriodDayRow? = rows[date]

        override suspend fun upsertPeriodDay(
            date: LocalDate,
            flowLevel: PeriodFlowLevel?,
            hasClots: Boolean,
            updatedAtEpochMs: Long,
        ) {
            rows[date] =
                PeriodDayRow(
                    logDate = date,
                    flowLevel = flowLevel,
                    hasClots = hasClots,
                    updatedAtEpochMs = updatedAtEpochMs,
                )
        }

        override suspend fun periodDaysInRange(
            start: LocalDate,
            end: LocalDate,
        ): List<PeriodDayRow> = rows.values.filter { !it.logDate.isBefore(start) && !it.logDate.isAfter(end) }
    }

    private class FakeTombstones(
        private val deleted: Set<LocalDate> = emptySet(),
    ) : PeriodDayTombstoneStore {
        override suspend fun isUserDeleted(date: LocalDate): Boolean = date in deleted

        override suspend fun userDeletedDatesInRange(
            start: LocalDate,
            end: LocalDate,
        ): Set<LocalDate> = deleted.filter { !it.isBefore(start) && !it.isAfter(end) }.toSet()
    }

    private class RecordingRemoteClient(
        private val granted: Set<String> = setOf("read", "write"),
        private val readResult: List<HealthConnectMenstruationFlowDay> = emptyList(),
        private val throwOnRead: Boolean = false,
    ) : HealthConnectRemoteClient {
        var callCount = 0
        var lastExport: List<MenstruationFlowExportWrite> = emptyList()
        var lastDeletedIds: List<String> = emptyList()

        override suspend fun availability(): HealthConnectAvailability {
            callCount++
            return HealthConnectAvailability.READY
        }

        override suspend fun grantedPermissionStrings(): HealthConnectClientResult<Set<String>> {
            callCount++
            return HealthConnectClientResult.Ok(granted)
        }

        override suspend fun readMenstruationFlowDays(): MenstruationFlowDaysResult {
            callCount++
            if (throwOnRead) return HealthConnectClientResult.Error("Boom")
            return HealthConnectClientResult.Ok(readResult)
        }

        override suspend fun upsertMenstruationFlowExports(
            exports: List<MenstruationFlowExportWrite>,
        ): HealthConnectClientResult<Int> {
            callCount++
            lastExport = exports
            return HealthConnectClientResult.Ok(exports.size)
        }

        override suspend fun deleteMenstruationByClientRecordIds(
            clientRecordIds: List<String>,
        ): HealthConnectClientResult<Int> {
            callCount++
            lastDeletedIds = clientRecordIds
            return HealthConnectClientResult.Ok(clientRecordIds.size)
        }
    }
}
