package dev.mahin.android.cycle

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.common.FixedLocalDateClock
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.datastore.CalendarUiPreferencesRepository
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.MainDispatcherRule
import dev.mahin.core.testing.ViewModelStoreTestHarness
import java.time.LocalDate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CalendarViewModelLegendTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule =
        MainDispatcherRule(
            testDispatcher,
            beforeResetMain = { viewModelStore.clear() },
        )

    private lateinit var database: MahinDatabase
    private lateinit var cycleRepository: CycleTrackingRepository
    private lateinit var calendarPrefs: CalendarUiPreferencesRepository
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
        calendarPrefs = CalendarUiPreferencesRepository(context)
        runBlocking { seedProfile() }
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        runBlocking { calendarPrefs.clear() }
        database.close()
    }

    @Test
    fun firstDateTap_autoCollapsesLegendAndSetsAutoCollapsedOnce() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            viewModel.uiState.filter { it.legendExpanded }.first()
            viewModel.selectJalaliDate(JalaliDate(1403, 12, 15))
            viewModel.uiState.filter { !it.legendExpanded && it.legendAutoCollapsedOnce }.first()
        }

    @Test
    fun toggleLegend_expandsAfterAutoCollapse() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            viewModel.selectJalaliDate(JalaliDate(1403, 12, 10))
            viewModel.uiState.filter { !it.legendExpanded }.first()
            viewModel.toggleLegendExpanded()
            viewModel.uiState.filter { it.legendExpanded }.first()
        }

    @Test
    fun secondDateTap_leavesLegendExpanded() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            viewModel.selectJalaliDate(JalaliDate(1403, 12, 10))
            viewModel.uiState.filter { !it.legendExpanded }.first()
            idle()
            assertThat(calendarPrefs.observeLegendAutoCollapsedOnce().first()).isTrue()
            assertThat(calendarPrefs.observeLegendCollapsed().first()).isTrue()
            viewModel.toggleLegendExpanded()
            viewModel.uiState.filter { it.legendExpanded }.first()
            assertThat(calendarPrefs.observeLegendCollapsed().first()).isFalse()
            viewModel.selectJalaliDate(JalaliDate(1403, 12, 11))
            idle()
            assertThat(viewModel.uiState.value.legendExpanded).isTrue()
            assertThat(calendarPrefs.observeLegendAutoCollapsedOnce().first()).isTrue()
            assertThat(calendarPrefs.observeLegendCollapsed().first()).isFalse()
        }

    @Test
    fun newViewModelInstance_keepsLegendExpandedAfterUserReExpand() =
        runTest(testDispatcher) {
            val first = createViewModel()
            first.selectJalaliDate(JalaliDate(1403, 12, 10))
            first.uiState.filter { !it.legendExpanded && it.legendAutoCollapsedOnce }.first()
            first.toggleLegendExpanded()
            first.uiState.filter { it.legendExpanded }.first()
            viewModelStore.clear()
            idle()
            val second = createViewModel()
            second.uiState.filter { it.legendExpanded && it.legendAutoCollapsedOnce }.first()
        }

    private fun createViewModel(): CalendarViewModel =
        viewModelStore.hold(
            CalendarViewModel(
                repository = cycleRepository,
                calendarUiPreferencesRepository = calendarPrefs,
                localDateClock = FixedLocalDateClock(LocalDate.of(2025, 3, 6)),
            ),
        )

    private suspend fun seedProfile() {
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
    }

    private fun idle() {
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()
    }
}
