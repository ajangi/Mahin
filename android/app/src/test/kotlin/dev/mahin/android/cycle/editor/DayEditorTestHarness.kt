package dev.mahin.android.cycle.editor

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.mahin.android.testing.datastore.LoggingPreferencesTestHandle
import dev.mahin.core.common.FixedLocalDateClock
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.database.logging.CustomSymptomTagRepository
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.database.ttc.TtcTrackingRepository
import dev.mahin.core.datastore.HealthConnectPeriodDayTombstoneRepository
import dev.mahin.core.datastore.LoggingPreferencesRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.datastore.TtcPrivacyPreferencesRepository
import dev.mahin.core.healthconnect.HealthConnectPeriodDayIntegrationGate
import dev.mahin.core.healthconnect.PeriodDayTrackingService
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.ViewModelStoreTestHarness
import dev.mahin.core.testing.buildMahinInMemoryDatabase
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.robolectric.shadows.ShadowLooper

@Suppress("LongParameterList")
internal class DayEditorTestHarness private constructor(
    val cycleRepository: CycleTrackingRepository,
    val ttcRepository: TtcTrackingRepository,
    val loggingPreferencesRepository: LoggingPreferencesRepository,
    private val ttcPrivacyRepository: TtcPrivacyPreferencesRepository,
    private val periodDayTrackingService: PeriodDayTrackingService,
    val customSymptomTagRepository: CustomSymptomTagRepository,
    val tombstoneRepository: HealthConnectPeriodDayTombstoneRepository,
    private val localDateClock: FixedLocalDateClock,
    private val viewModelStore: ViewModelStoreTestHarness,
    val database: MahinDatabase,
    private val loggingPreferencesHandle: LoggingPreferencesTestHandle,
) {
    fun dailyLogFor(date: LocalDate): DailyLogEntity? = runBlocking { database.dailyLogDao().getForDate(date) }

    fun periodDayFor(date: LocalDate) = runBlocking { database.periodDayDao().getForDate(date) }

    fun createViewModel(healthConnectActive: Boolean = false): DayEditorViewModel {
        val gate =
            object : HealthConnectPeriodDayIntegrationGate {
                override suspend fun isIntegrationActive(): Boolean = healthConnectActive
            }
        return viewModelStore.hold(
            DayEditorViewModel(
                cycleRepository = cycleRepository,
                ttcRepository = ttcRepository,
                pregnancyRepository = pregnancyRepository(),
                loggingPreferencesRepository = loggingPreferencesRepository,
                ttcPrivacyRepository = ttcPrivacyRepository,
                periodDayTrackingService = periodDayTrackingService,
                healthConnectPeriodDayIntegrationGate = gate,
                customSymptomTagRepository = customSymptomTagRepository,
                localDateClock = localDateClock,
            ),
        )
    }

    private fun pregnancyRepository(): PregnancyTrackingRepository =
        PregnancyTrackingRepository(
            database,
            PregnancyTimerPreferencesRepository(ApplicationProvider.getApplicationContext()),
        )

    suspend fun seedCycleProfile(mode: ReproductiveMode = ReproductiveMode.CYCLE_TRACKING) {
        database.cycleProfileDao().upsert(
            CycleProfileEntity(
                reproductiveMode = mode,
                typicalCycleLengthDays = 28,
                typicalPeriodLengthDays = 5,
                regularity = CycleRegularity.UNKNOWN,
                onboardingCompleted = true,
                updatedAtEpochMs = 0L,
            ),
        )
    }

    fun close() {
        viewModelStore.clear()
        DayEditorTestVmRegistry.unregister(viewModelStore)
        ShadowLooper.idleMainLooper()
        runBlocking { tombstoneRepository.clearAll() }
        database.close()
        loggingPreferencesHandle.close()
    }

    companion object {
        fun create(today: LocalDate): DayEditorTestHarness {
            DayEditorTestVmRegistry.assertAllStoresClosed()
            val context = ApplicationProvider.getApplicationContext<Context>()
            val database = buildMahinInMemoryDatabase(context)
            val cycleRepository = CycleTrackingRepository(database)
            val ttcRepository = TtcTrackingRepository(database)
            val tombstoneRepository = HealthConnectPeriodDayTombstoneRepository(context)
            val loggingPreferencesHandle =
                LoggingPreferencesTestHandle.openInDirectory(
                    java.nio.file.Files
                        .createTempDirectory("mahin-day-editor-logging-prefs")
                        .toFile(),
                )
            val loggingPreferencesRepository = loggingPreferencesHandle.repository
            runBlocking {
                loggingPreferencesRepository.clear()
                tombstoneRepository.clearAll()
            }
            ShadowLooper.idleMainLooper()
            val viewModelStore = ViewModelStoreTestHarness()
            DayEditorTestVmRegistry.register("DayEditorTestHarness", viewModelStore)
            return DayEditorTestHarness(
                cycleRepository = cycleRepository,
                ttcRepository = ttcRepository,
                loggingPreferencesRepository = loggingPreferencesRepository,
                ttcPrivacyRepository = TtcPrivacyPreferencesRepository(context),
                periodDayTrackingService = PeriodDayTrackingService(cycleRepository, tombstoneRepository),
                customSymptomTagRepository = CustomSymptomTagRepository(database),
                tombstoneRepository = tombstoneRepository,
                localDateClock = FixedLocalDateClock(today),
                viewModelStore = viewModelStore,
                database = database,
                loggingPreferencesHandle = loggingPreferencesHandle,
            )
        }
    }
}
