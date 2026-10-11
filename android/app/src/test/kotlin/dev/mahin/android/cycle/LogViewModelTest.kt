package dev.mahin.android.cycle

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.android.navigation.LogTabDateRequest
import dev.mahin.core.common.FixedLocalDateClock
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.database.ttc.TtcTrackingRepository
import dev.mahin.core.datastore.HealthConnectPeriodDayTombstoneRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.datastore.TtcPrivacyPreferencesRepository
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.healthconnect.HealthConnectPeriodDayIntegrationGate
import dev.mahin.core.healthconnect.PeriodDayTrackingService
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.ViewModelStoreTestHarness
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
    private val localDateClock = FixedLocalDateClock(LocalDate.of(2025, 4, 10))
    private lateinit var database: MahinDatabase
    private lateinit var cycleRepository: CycleTrackingRepository
    private lateinit var ttcRepository: TtcTrackingRepository
    private lateinit var pregnancyRepository: PregnancyTrackingRepository
    private lateinit var privacyRepository: TtcPrivacyPreferencesRepository
    private lateinit var periodDayTrackingService: PeriodDayTrackingService
    private lateinit var tombstoneRepository: HealthConnectPeriodDayTombstoneRepository
    private val viewModelStore = ViewModelStoreTestHarness()

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
        ttcRepository = TtcTrackingRepository(database)
        pregnancyRepository = PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
        privacyRepository = TtcPrivacyPreferencesRepository(context)
        tombstoneRepository = HealthConnectPeriodDayTombstoneRepository(context)
        periodDayTrackingService = PeriodDayTrackingService(cycleRepository, tombstoneRepository)
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        database.close()
    }

    @Test
    fun pendingLogTabDate_selectsDateAndClearsRequest() {
        runBlocking {
            seedTtcProfile()
            val target = LocalDate.of(2025, 4, 10)
            val request = LogTabDateRequest()
            request.request(target)
            val vm = createViewModel(integrationActive = false, logTabDateRequest = request)
            awaitUntil {
                vm.uiState.value.selectedJalali == PersianCivilDateConverter.toJalali(target)
            }
            assertThat(request.pendingDate.value).isNull()
        }
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
                    medicationTags = "",
                    localCustomSymptomTags = "",
                    painSeverity = null,
                    painLocation = null,
                    painNote = null,
                    note = "یادداشت الف",
                    sleepLogged = false,
                    exerciseLogged = false,
                    waterGlasses = 0,
                    stressLevel = null,
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
            val savedA = database.dailyLogDao().getForDate(dayA)
            assertThat(savedA?.symptomTags).isEqualTo("سردرد")
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

    private fun createViewModel(
        integrationActive: Boolean,
        logTabDateRequest: LogTabDateRequest = LogTabDateRequest(),
    ): LogViewModel {
        val gate =
            object : HealthConnectPeriodDayIntegrationGate {
                override suspend fun isIntegrationActive(): Boolean = integrationActive
            }
        return viewModelStore.hold(
            LogViewModel(
                cycleRepository,
                periodDayTrackingService,
                gate,
                ttcRepository,
                pregnancyRepository,
                privacyRepository,
                logTabDateRequest,
                localDateClock,
            ),
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
