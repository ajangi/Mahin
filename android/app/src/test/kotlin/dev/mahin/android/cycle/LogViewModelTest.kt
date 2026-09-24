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
import dev.mahin.core.database.ttc.TtcTrackingRepository
import dev.mahin.core.datastore.TtcPrivacyPreferencesRepository
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LogViewModelTest {
    private lateinit var database: MahinDatabase
    private lateinit var cycleRepository: CycleTrackingRepository
    private lateinit var ttcRepository: TtcTrackingRepository
    private lateinit var privacyRepository: TtcPrivacyPreferencesRepository

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
        privacyRepository = TtcPrivacyPreferencesRepository(context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun switchingDate_resetsSymptomsBeforeSave() {
        runBlocking {
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
            val vm = LogViewModel(cycleRepository, ttcRepository, privacyRepository)
            vm.onDateSelected(PersianCivilDateConverter.toJalali(dayB))
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
            val vm = LogViewModel(cycleRepository, ttcRepository, privacyRepository)
            vm.onDateSelected(PersianCivilDateConverter.toJalali(day))
            val state = vm.uiState.value.copy(bbtInput = "۳۶٫abc")
            vm.performSave(state)
            assertThat(vm.uiState.value.bbtError).isEqualTo(BbtFieldError.UNPARSEABLE)
            assertThat(database.ttcDayLogDao().getForDate(day)?.bbtCelsius).isEqualTo(36.6)
        }
    }
}
