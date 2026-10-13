package dev.mahin.android.cycle

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.common.FixedLocalDateClock
import dev.mahin.core.content.ContentDailyTipDto
import dev.mahin.core.content.DailyTipTargetingKey
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.CalendarUiPreferencesRepository
import dev.mahin.core.datastore.ContentCachePreferencesRepository
import dev.mahin.core.datastore.NotificationPreferencesRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.PregnancyOutcome
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.ViewModelStoreTestHarness
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
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
    private val fixedToday = LocalDate.of(2025, 6, 15)
    private val localDateClock = FixedLocalDateClock(fixedToday)
    private lateinit var database: MahinDatabase
    private lateinit var cycleRepository: CycleTrackingRepository
    private lateinit var pregnancyRepository: PregnancyTrackingRepository
    private lateinit var contentCachePreferences: ContentCachePreferencesRepository
    private val dailyTipDataSource = FakeDailyTipDataSource()
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
        pregnancyRepository = PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
        contentCachePreferences = ContentCachePreferencesRepository(context)
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            NotificationPreferencesRepository(context).clear()
            CalendarUiPreferencesRepository(context).clear()
            contentCachePreferences.clearCachedArticles()
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
            val viewModel = createViewModel()
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
            val viewModel = createViewModel()
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
            val viewModel = createViewModel()
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
            val viewModel = createViewModel()
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
            val viewModel = createViewModel()
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
                    medicationTags = "",
                    localCustomSymptomTags = "",
                    painSeverity = null,
                    painLocation = null,
                    painNote = null,
                    note = null,
                    sleepLogged = false,
                    exerciseLogged = false,
                    waterGlasses = 0,
                    stressLevel = null,
                    updatedAtEpochMs = 0L,
                ),
            )
            val viewModel = createViewModel()
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
            val viewModel = createViewModel()
            pregnancyRepository.recordOutcome(
                pregnancyId = pregnancy.id,
                outcome = PregnancyOutcome.PREGNANCY_LOSS,
                wantsSupportContent = true,
            )
            awaitUntil { viewModel.uiState.value.postTransitionLearnLinkVisible }
        }
    }

    @Test
    fun dailyTip_hiddenWhenApiReturnsNothingAndNoCache() {
        runBlocking {
            seedCycleProfile()
            dailyTipDataSource.tips = emptyList()
            val viewModel = createViewModel()
            awaitUntil { dailyTipDataSource.fetchCount > 0 }
            assertThat(viewModel.uiState.value.dailyTip).isNull()
        }
    }

    @Test
    fun dailyTip_hiddenWhenOfflineWithoutCache() {
        runBlocking {
            seedCycleProfile()
            dailyTipDataSource.throwOnFetch = true
            val viewModel = createViewModel()
            awaitUntil { dailyTipDataSource.fetchCount > 0 }
            assertThat(viewModel.uiState.value.dailyTip).isNull()
        }
    }

    @Test
    fun dailyTip_hiddenWhenApiEmptyDespiteStaleCache() {
        runBlocking {
            seedCycleProfile()
            val staleTip =
                ContentDailyTipDto(
                    id = "stale",
                    slug = "stale",
                    locale = "fa-IR",
                    title = "قدیمی",
                    medicalRiskLevel = "none",
                    contentVersion = 1,
                )
            val key = expectedDailyTipKey()
            dailyTipDataSource.seedCache(staleTip, key)
            dailyTipDataSource.tips = emptyList()
            val viewModel = createViewModel()
            awaitUntil { dailyTipDataSource.fetchCount > 0 }
            assertThat(viewModel.uiState.value.dailyTip).isNull()
            assertThat(dailyTipDataSource.cachedDailyTipFor(key)).isNull()
        }
    }

    @Test
    fun dailyTip_shownWhenOfflineWithMatchingCache() {
        runBlocking {
            seedCycleProfile()
            val cachedTip =
                ContentDailyTipDto(
                    id = "cached",
                    slug = "cached",
                    locale = "fa-IR",
                    title = "ذخیره‌شده",
                    medicalRiskLevel = "none",
                    contentVersion = 1,
                )
            dailyTipDataSource.seedCache(cachedTip, expectedDailyTipKey())
            dailyTipDataSource.throwOnFetch = true
            val viewModel = createViewModel()
            awaitUntil { viewModel.uiState.value.dailyTip != null }
            assertThat(
                viewModel.uiState.value.dailyTip
                    ?.title,
            ).isEqualTo("ذخیره‌شده")
        }
    }

    @Test
    fun dailyTip_showsFromApiWhenAvailable() {
        runBlocking {
            seedCycleProfile()
            dailyTipDataSource.tips =
                listOf(
                    ContentDailyTipDto(
                        id = "tip-1",
                        slug = "tip",
                        locale = "fa-IR",
                        title = "نکته",
                        medicalRiskLevel = "none",
                        contentVersion = 1,
                    ),
                )
            val viewModel = createViewModel()
            awaitUntil { viewModel.uiState.value.dailyTip != null }
            assertThat(
                viewModel.uiState.value.dailyTip
                    ?.title,
            ).isEqualTo("نکته")
        }
    }

    private fun createViewModel(): TodayViewModel =
        viewModelStore.hold(
            TodayViewModel(
                cycleRepository,
                pregnancyRepository,
                notificationPrefs(),
                dailyTipDataSource,
                contentCachePreferences,
                localDateClock,
            ),
        )

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

    private suspend fun expectedDailyTipKey(): DailyTipTargetingKey {
        val dashboard = cycleRepository.observeDashboard(fixedToday).first()
        val cyclePhase =
            DailyTipTargetingMapper.cyclePhase(
                cycleDay = dashboard.prediction.cycleDay,
                onPeriodToday = dashboard.onPeriodToday,
                inFertileWindow =
                    dashboard.prediction.fertileWindow?.let { range ->
                        val today = fixedToday
                        !today.isBefore(range.earliest) && !today.isAfter(range.latest)
                    } == true,
            )
        return DailyTipTargetingKey(
            mode = "cycle",
            cyclePhase = cyclePhase,
            pregnancyWeek = null,
            localDateIso = fixedToday.toString(),
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
