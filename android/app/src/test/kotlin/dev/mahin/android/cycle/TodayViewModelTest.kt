package dev.mahin.android.cycle

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.CalendarUiPreferencesRepository
import dev.mahin.core.datastore.NotificationPreferencesRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.PregnancyOutcome
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate
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
class TodayViewModelTest {
    private lateinit var database: MahinDatabase
    private lateinit var cycleRepository: CycleTrackingRepository
    private lateinit var pregnancyRepository: PregnancyTrackingRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        cycleRepository = CycleTrackingRepository(database)
        pregnancyRepository = PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
    }

    @After
    fun tearDown() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            NotificationPreferencesRepository(context).clear()
            CalendarUiPreferencesRepository(context).clear()
        }
        database.close()
    }

    @Test
    fun recordOutcome_exposesPostPregnancyTransition() {
        runBlocking {
            seedPregnantProfile()
            val pregnancy =
                pregnancyRepository.startPregnancy(
                    lmpDate = LocalDate.of(2025, 1, 1),
                    clinicalEddDate = null,
                    datingReason = null,
                )
            val viewModel = TodayViewModel(cycleRepository, pregnancyRepository, notificationPrefs())
            pregnancyRepository.recordOutcome(
                pregnancyId = pregnancy.id,
                outcome = PregnancyOutcome.PREGNANCY_LOSS,
                wantsSupportContent = null,
            )
            awaitUntil { viewModel.uiState.value.postPregnancyTransition }
            assertThat(viewModel.uiState.value.reproductiveMode)
                .isEqualTo(ReproductiveMode.POST_PREGNANCY_TRANSITION)
        }
    }

    @Test
    fun resumeCycleTracking_leavesPostTransitionMode() {
        runBlocking {
            seedPregnantProfile()
            val pregnancy =
                pregnancyRepository.startPregnancy(
                    lmpDate = LocalDate.of(2025, 1, 1),
                    clinicalEddDate = null,
                    datingReason = null,
                )
            val viewModel = TodayViewModel(cycleRepository, pregnancyRepository, notificationPrefs())
            pregnancyRepository.recordOutcome(
                pregnancyId = pregnancy.id,
                outcome = PregnancyOutcome.PREGNANCY_LOSS,
                wantsSupportContent = null,
            )
            awaitUntil { viewModel.uiState.value.postPregnancyTransition }
            viewModel.resumeCycleTracking()
            awaitUntil { viewModel.uiState.value.reproductiveMode == ReproductiveMode.CYCLE_TRACKING }
            assertThat(viewModel.uiState.value.postPregnancyTransition).isFalse()
        }
    }

    @Test
    fun resumeTtc_leavesPostTransitionMode() {
        runBlocking {
            seedPregnantProfile()
            val pregnancy =
                pregnancyRepository.startPregnancy(
                    lmpDate = LocalDate.of(2025, 1, 1),
                    clinicalEddDate = null,
                    datingReason = null,
                )
            val viewModel = TodayViewModel(cycleRepository, pregnancyRepository, notificationPrefs())
            pregnancyRepository.recordOutcome(
                pregnancyId = pregnancy.id,
                outcome = PregnancyOutcome.PREGNANCY_LOSS,
                wantsSupportContent = null,
            )
            awaitUntil { viewModel.uiState.value.postPregnancyTransition }
            viewModel.resumeTtc()
            awaitUntil { viewModel.uiState.value.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE }
            assertThat(viewModel.uiState.value.postPregnancyTransition).isFalse()
        }
    }

    @Test
    fun postTransitionLearnLinkHidden_whenSupportContentFalse() =
        runBlocking {
            seedPregnantProfile()
            val pregnancy =
                pregnancyRepository.startPregnancy(
                    lmpDate = LocalDate.of(2025, 1, 1),
                    clinicalEddDate = null,
                    datingReason = null,
                )
            val viewModel = TodayViewModel(cycleRepository, pregnancyRepository, notificationPrefs())
            pregnancyRepository.recordOutcome(
                pregnancyId = pregnancy.id,
                outcome = PregnancyOutcome.PREGNANCY_LOSS,
                wantsSupportContent = false,
            )
            awaitUntil { viewModel.uiState.value.postPregnancyTransition }
            assertThat(viewModel.uiState.value.postTransitionLearnLinkVisible).isFalse()
        }

    @Test
    fun onWeekDaySelected_marksSelectedDayInWeekStripWeeks() =
        runBlocking {
            seedCycleProfile()
            val viewModel = TodayViewModel(cycleRepository, pregnancyRepository, notificationPrefs())
            awaitUntil {
                val ui = viewModel.uiState.value
                ui.weekStripWeeks.isNotEmpty()
            }
            val weeks = viewModel.uiState.value.weekStripWeeks
            val target = weeks.first()[3]
            viewModel.onWeekDaySelected(target.date)
            awaitUntil {
                val stripWeeks = viewModel.uiState.value.weekStripWeeks
                val flat = stripWeeks.flatten()
                flat.any { it.date == target.date && it.isSelected }
            }
            val selectedCount =
                viewModel.uiState.value.weekStripWeeks
                    .flatten()
                    .count { it.isSelected }
            assertThat(selectedCount).isEqualTo(1)
        }

    @Test
    fun onWeekDaySelected_populatesDaySheetLogLines() =
        runBlocking {
            seedCycleProfile()
            val logDate = LocalDate.now()
            database.dailyLogDao().upsert(
                dev.mahin.core.database.entity.DailyLogEntity(
                    id = "log-1",
                    logDate = logDate,
                    moodTags = "شاد",
                    symptomTags = "خستگی",
                    painSeverity = null,
                    note = null,
                    updatedAtEpochMs = 0L,
                ),
            )
            val viewModel = TodayViewModel(cycleRepository, pregnancyRepository, notificationPrefs())
            awaitUntil {
                val ui = viewModel.uiState.value
                ui.weekStripWeeks.isNotEmpty()
            }
            viewModel.onWeekDaySelected(logDate)
            awaitUntil {
                val ui = viewModel.uiState.value
                ui.daySheetLogLines.isNotEmpty()
            }
            val logLines = viewModel.uiState.value.daySheetLogLines
            val firstLine = logLines.first()
            assertThat(firstLine).contains("خستگی")
        }

    @Test
    fun postTransitionLearnLinkVisible_whenSupportContentRequested() {
        runBlocking {
            seedPregnantProfile()
            val pregnancy =
                pregnancyRepository.startPregnancy(
                    lmpDate = LocalDate.of(2025, 1, 1),
                    clinicalEddDate = null,
                    datingReason = null,
                )
            val viewModel = TodayViewModel(cycleRepository, pregnancyRepository, notificationPrefs())
            pregnancyRepository.recordOutcome(
                pregnancyId = pregnancy.id,
                outcome = PregnancyOutcome.PREGNANCY_LOSS,
                wantsSupportContent = true,
            )
            awaitUntil { viewModel.uiState.value.postTransitionLearnLinkVisible }
        }
    }

    private fun notificationPrefs(): NotificationPreferencesRepository {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return NotificationPreferencesRepository(context)
    }

    private suspend fun seedCycleProfile() {
        database.cycleProfileDao().upsert(
            CycleProfileEntity(
                reproductiveMode = ReproductiveMode.CYCLE_TRACKING,
                typicalCycleLengthDays = 28,
                typicalPeriodLengthDays = 5,
                regularity = CycleRegularity.REGULAR,
                onboardingCompleted = true,
                updatedAtEpochMs = 0L,
            ),
        )
        val anchor = LocalDate.now().minusDays(13)
        database.periodRecordDao().upsert(
            dev.mahin.core.database.entity.PeriodRecordEntity(
                id = "period-1",
                startDate = anchor,
                endDate = anchor.plusDays(4),
                note = null,
                createdAtEpochMs = 0L,
                updatedAtEpochMs = 0L,
            ),
        )
    }

    private suspend fun seedPregnantProfile() {
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
    }

    private suspend fun awaitUntil(predicate: () -> Boolean) {
        repeat(200) {
            if (predicate()) return
            delay(10)
            ShadowLooper.idleMainLooper()
        }
        throw AssertionError("Condition not met")
    }
}
