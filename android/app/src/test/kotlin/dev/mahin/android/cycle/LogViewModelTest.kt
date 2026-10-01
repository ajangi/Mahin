package dev.mahin.android.cycle

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.database.entity.TtcDayLogEntity
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.database.ttc.TtcTrackingRepository
import dev.mahin.core.datastore.HealthConnectPeriodDayTombstoneRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.datastore.TtcPrivacyPreferencesRepository
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.healthconnect.HealthConnectPeriodDayIntegrationGate
import dev.mahin.core.healthconnect.PeriodDayTrackingService
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.PeriodFlowLevel
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LogViewModelTest {
    private lateinit var database: MahinDatabase
    private lateinit var cycleRepository: CycleTrackingRepository
    private lateinit var ttcRepository: TtcTrackingRepository
    private lateinit var pregnancyRepository: PregnancyTrackingRepository
    private lateinit var privacyRepository: TtcPrivacyPreferencesRepository
    private lateinit var periodDayTrackingService: PeriodDayTrackingService
    private lateinit var tombstoneRepository: HealthConnectPeriodDayTombstoneRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        cycleRepository = CycleTrackingRepository(database)
        ttcRepository = TtcTrackingRepository(database)
        pregnancyRepository = PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
        privacyRepository = TtcPrivacyPreferencesRepository(context)
        tombstoneRepository = HealthConnectPeriodDayTombstoneRepository(context)
        periodDayTrackingService = PeriodDayTrackingService(cycleRepository, tombstoneRepository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun switchingDate_resetsSymptomsBeforeSave() {
        runBlocking {
            seedTtcProfile()
            val dayA = LocalDate.of(2025, 3, 1)
            val dayB = LocalDate.of(2025, 3, 2)
            database.dailyLogDao().upsert(
                DailyLogEntity(
                    id = UUID.randomUUID().toString(),
                    logDate = dayA,
                    moodTags = "",
                    symptomTags = "سردرد",
                    painSeverity = null,
                    note = "یادداشت الف",
                    updatedAtEpochMs = 0L,
                ),
            )
            val vm = createViewModel(integrationActive = false)
            vm.onDateSelected(PersianCivilDateConverter.toJalali(dayA))
            awaitUntil {
                val state = vm.uiState.value
                state.selectedDateReady && state.symptomTags.contains("سردرد")
            }
            vm.onDateSelected(PersianCivilDateConverter.toJalali(dayB))
            awaitUntil {
                val state = vm.uiState.value
                state.selectedDateReady && state.symptomTags.isEmpty() && state.note.isEmpty()
            }
            assertThat(vm.uiState.value.symptomTags).isEmpty()
            vm.toggleSymptom("نفخ")
            vm.performSave(vm.uiState.value)
            val savedB = database.dailyLogDao().getForDate(dayB)
            assertThat(savedB?.symptomTags).isEqualTo("نفخ")
            val savedA = database.dailyLogDao().getForDate(dayA)
            assertThat(savedA?.symptomTags).isEqualTo("سردرد")
        }
    }

    @Test
    fun save_withInvalidPersianBbt_blocksSaveAndPreservesStoredValue() {
        runBlocking {
            seedTtcProfile()
            val day = LocalDate.of(2025, 3, 5)
            database.ttcDayLogDao().upsert(
                TtcDayLogEntity(
                    id = "t1",
                    logDate = day,
                    bbtCelsius = 36.6,
                    ovulationTestResult = null,
                    cervicalMucus = null,
                    intercourseLogged = false,
                    intercourseProtected = null,
                    pregnancyTestResult = null,
                    updatedAtEpochMs = 0L,
                ),
            )
            val vm = createViewModel(integrationActive = false)
            vm.onDateSelected(PersianCivilDateConverter.toJalali(day))
            awaitUntil { vm.uiState.value.selectedDateReady }
            val state = vm.uiState.value.copy(bbtInput = "۳۶٫abc")
            vm.performSave(state)
            assertThat(vm.uiState.value.bbtError).isEqualTo(BbtFieldError.UNPARSEABLE)
            assertThat(database.ttcDayLogDao().getForDate(day)?.bbtCelsius).isEqualTo(36.6)
        }
    }

    @Test
    fun save_withIntercourseOptInOff_preservesStoredIntercourse() {
        runBlocking {
            seedTtcProfile()
            val day = LocalDate.of(2025, 3, 8)
            database.ttcDayLogDao().upsert(
                TtcDayLogEntity(
                    id = "t2",
                    logDate = day,
                    bbtCelsius = null,
                    ovulationTestResult = null,
                    cervicalMucus = null,
                    intercourseLogged = true,
                    intercourseProtected = true,
                    pregnancyTestResult = null,
                    updatedAtEpochMs = 0L,
                ),
            )
            val vm = createViewModel(integrationActive = false)
            vm.onDateSelected(PersianCivilDateConverter.toJalali(day))
            awaitUntil { vm.uiState.value.selectedDateReady && vm.uiState.value.intercourseLogged }
            assertThat(vm.uiState.value.intercourseLoggingEnabled).isFalse()
            vm.performSave(vm.uiState.value.copy(note = "یادداشت"))
            val stored = database.ttcDayLogDao().getForDate(day)
            assertThat(stored?.intercourseLogged).isTrue()
            assertThat(stored?.intercourseProtected).isTrue()
        }
    }

    @Test
    fun healthConnectOff_untickPeriod_doesNotDeletePeriodDay() {
        runBlocking {
            val day = LocalDate.of(2025, 5, 1)
            cycleRepository.upsertPeriodDay(day, PeriodFlowLevel.LIGHT, hasClots = false)
            val vm = createViewModel(integrationActive = false)
            vm.onDateSelected(PersianCivilDateConverter.toJalali(day))
            awaitUntil { vm.uiState.value.selectedDateReady && vm.uiState.value.loggingPeriod }
            vm.toggleLoggingPeriod()
            vm.performSave(vm.uiState.value)
            assertThat(cycleRepository.getPeriodDayForDate(day)).isNotNull()
            assertThat(tombstoneRepository.isUserDeleted(day)).isFalse()
        }
    }

    @Test
    fun healthConnectOn_untickPeriod_deletesRowAndWritesTombstone() {
        runBlocking {
            val day = LocalDate.of(2025, 5, 2)
            cycleRepository.upsertPeriodDay(day, PeriodFlowLevel.MEDIUM, hasClots = false)
            val vm = createViewModel(integrationActive = true)
            vm.onDateSelected(PersianCivilDateConverter.toJalali(day))
            awaitUntil { vm.uiState.value.selectedDateReady && vm.uiState.value.loggingPeriod }
            vm.toggleLoggingPeriod()
            vm.performSave(vm.uiState.value)
            assertThat(cycleRepository.getPeriodDayForDate(day)).isNull()
            assertThat(tombstoneRepository.isUserDeleted(day)).isTrue()
        }
    }

    @Test
    fun healthConnectOn_relogPeriod_clearsTombstone() {
        runBlocking {
            val day = LocalDate.of(2025, 5, 3)
            tombstoneRepository.markUserDeleted(day)
            val vm = createViewModel(integrationActive = true)
            vm.onDateSelected(PersianCivilDateConverter.toJalali(day))
            awaitUntil { vm.uiState.value.selectedDateReady && !vm.uiState.value.loggingPeriod }
            vm.toggleLoggingPeriod()
            vm.onFlowLevelSelected(PeriodFlowLevel.HEAVY)
            vm.performSave(vm.uiState.value)
            assertThat(tombstoneRepository.isUserDeleted(day)).isFalse()
            assertThat(cycleRepository.getPeriodDayForDate(day)?.flowLevel).isEqualTo(PeriodFlowLevel.HEAVY)
        }
    }

    @Test
    fun save_beforeDateLoaded_isIgnored() {
        runBlocking {
            seedTtcProfile()
            val vm = createViewModel(integrationActive = false)
            val day = LocalDate.of(2025, 6, 1)
            vm.performSave(
                vm.uiState.value.copy(
                    selectedJalali = PersianCivilDateConverter.toJalali(day),
                    selectedDateReady = false,
                    note = "blocked",
                ),
            )
            assertThat(database.dailyLogDao().getForDate(day)).isNull()
        }
    }

    private suspend fun seedTtcProfile() {
        database.cycleProfileDao().upsert(
            CycleProfileEntity(
                reproductiveMode = ReproductiveMode.TRYING_TO_CONCEIVE,
                typicalCycleLengthDays = 28,
                typicalPeriodLengthDays = 5,
                regularity = CycleRegularity.UNKNOWN,
                onboardingCompleted = true,
                updatedAtEpochMs = 0L,
            ),
        )
    }

    private fun createViewModel(integrationActive: Boolean): LogViewModel {
        val gate =
            object : HealthConnectPeriodDayIntegrationGate {
                override suspend fun isIntegrationActive(): Boolean = integrationActive
            }
        return LogViewModel(
            cycleRepository,
            periodDayTrackingService,
            gate,
            ttcRepository,
            pregnancyRepository,
            privacyRepository,
        )
    }

    private suspend fun awaitUntil(
        timeoutMs: Long = 2_000,
        condition: () -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            ShadowLooper.idleMainLooper()
            delay(25)
        }
        throw AssertionError("Condition not met within ${timeoutMs}ms")
    }
}
