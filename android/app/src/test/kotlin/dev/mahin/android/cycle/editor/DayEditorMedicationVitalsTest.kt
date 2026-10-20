package dev.mahin.android.cycle.editor

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.mahin.android.testing.datastore.LoggingPreferencesTestHandle
import dev.mahin.core.common.FixedLocalDateClock
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.logging.CustomSymptomTagRepository
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.database.ttc.TtcTrackingRepository
import dev.mahin.core.datastore.HealthConnectPeriodDayTombstoneRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.datastore.TtcPrivacyPreferencesRepository
import dev.mahin.core.healthconnect.HealthConnectPeriodDayIntegrationGate
import dev.mahin.core.healthconnect.PeriodDayTrackingService
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.MainDispatcherRule
import dev.mahin.core.testing.ViewModelStoreTestHarness
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DayEditorMedicationVitalsTest {
    private val testDispatcher = StandardTestDispatcher()
    private val loggingPreferencesDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var loggingPreferencesHandle: LoggingPreferencesTestHandle
    private lateinit var database: MahinDatabase
    private lateinit var cycleRepository: CycleTrackingRepository
    private lateinit var pregnancyRepository: PregnancyTrackingRepository
    private lateinit var viewModel: DayEditorViewModel
    private val viewModelStore = ViewModelStoreTestHarness()
    private val logDate = LocalDate.of(2025, 5, 10)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        database.openHelper.writableDatabase
        cycleRepository = CycleTrackingRepository(database)
        pregnancyRepository = PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
        loggingPreferencesHandle = LoggingPreferencesTestHandle.open(temporaryFolder, loggingPreferencesDispatcher)
        viewModel = createViewModel(context)
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        testDispatcher.scheduler.advanceUntilIdle()
        loggingPreferencesDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        ShadowLooper.idleMainLooper()
        loggingPreferencesHandle.closeAndCancelScope()
        database.close()
    }

    @Test
    fun medicationToggle_persistsToDailyLog() {
        runBlocking {
            viewModel.openDate(logDate)
            awaitUntil { viewModel.uiState.value.date == logDate }
            viewModel.toggleMedication("ویتامین")
            awaitUntil {
                cycleRepository.getDailyLogForDate(logDate)?.medicationTags?.contains("ویتامین") == true
            }
        }
    }

    @Test
    fun pregnancyVitals_persistWeightAndBloodPressure() {
        runBlocking {
            database.cycleProfileDao().upsert(
                CycleProfileEntity(
                    reproductiveMode = ReproductiveMode.PREGNANT,
                    typicalCycleLengthDays = 28,
                    typicalPeriodLengthDays = 5,
                    regularity = CycleRegularity.UNKNOWN,
                    onboardingCompleted = true,
                    updatedAtEpochMs = 0L,
                ),
            )
            pregnancyRepository.startPregnancy(
                lmpDate = logDate.minusWeeks(10),
                clinicalEddDate = null,
                datingReason = null,
            )
            viewModel.openDate(logDate)
            awaitUntil { viewModel.uiState.value.reproductiveMode == ReproductiveMode.PREGNANT }
            viewModel.onPregnancyWeightChange("65")
            viewModel.onPregnancyBpSystolicChange("120")
            viewModel.onPregnancyBpDiastolicChange("80")
            awaitUntil {
                val log = pregnancyRepository.getPregnancyDayLogForDate(logDate)
                log?.weightKg == 65.0 && log.bpSystolic == 120 && log.bpDiastolic == 80
            }
        }
    }

    private fun createViewModel(context: Context): DayEditorViewModel {
        val gate =
            object : HealthConnectPeriodDayIntegrationGate {
                override suspend fun isIntegrationActive(): Boolean = false
            }
        val factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    DayEditorViewModel(
                        cycleRepository = cycleRepository,
                        ttcRepository = TtcTrackingRepository(database),
                        pregnancyRepository = pregnancyRepository,
                        loggingPreferencesRepository = loggingPreferencesHandle.repository,
                        ttcPrivacyRepository = TtcPrivacyPreferencesRepository(context),
                        periodDayTrackingService =
                            PeriodDayTrackingService(
                                cycleRepository,
                                HealthConnectPeriodDayTombstoneRepository(context),
                            ),
                        healthConnectPeriodDayIntegrationGate = gate,
                        customSymptomTagRepository = CustomSymptomTagRepository(database),
                        localDateClock = FixedLocalDateClock(logDate),
                    ) as T
            }
        return viewModelStore.hold(DayEditorViewModel::class.java, factory)
    }

    private suspend fun awaitUntil(
        timeoutMs: Long = 6_000,
        condition: suspend () -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            testDispatcher.scheduler.advanceUntilIdle()
            loggingPreferencesDispatcher.scheduler.advanceUntilIdle()
            ShadowLooper.idleMainLooper()
        }
        throw AssertionError("Condition not met within ${timeoutMs}ms")
    }
}
