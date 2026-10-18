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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
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
    private val context: Context = ApplicationProvider.getApplicationContext()

    @get:Rule
    val mainDispatcherRule =
        MainDispatcherRule(
            testDispatcher,
            beforeResetMain = {
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
            },
        )

    @Test
    fun firstDateTap_autoCollapsesLegendAndSetsAutoCollapsedOnce() =
        runTest(testDispatcher) {
            withLegendHarness { viewModel, _, _, _ ->
                idle()
                viewModel.selectJalaliDate(JalaliDate(1403, 12, 15))
                idle()
                viewModel.uiState.filter { !it.legendExpanded && it.legendAutoCollapsedOnce }.first()
            }
        }

    @Test
    fun toggleLegend_expandsAfterAutoCollapse() =
        runTest(testDispatcher) {
            withLegendHarness { viewModel, _, _, _ ->
                idle()
                viewModel.selectJalaliDate(JalaliDate(1403, 12, 10))
                idle()
                viewModel.uiState.filter { !it.legendExpanded }.first()
                viewModel.toggleLegendExpanded()
                idle()
                viewModel.uiState.filter { it.legendExpanded }.first()
            }
        }

    @Test
    fun secondDateTap_leavesLegendExpanded() =
        runTest(testDispatcher) {
            withLegendHarness { viewModel, calendarPrefs, _, _ ->
                idle()
                viewModel.selectJalaliDate(JalaliDate(1403, 12, 10))
                idle()
                viewModel.uiState.filter { !it.legendExpanded }.first()
                assertThat(calendarPrefs.observeLegendAutoCollapsedOnce().first()).isTrue()
                assertThat(calendarPrefs.observeLegendCollapsed().first()).isTrue()
                viewModel.toggleLegendExpanded()
                idle()
                viewModel.uiState.filter { it.legendExpanded }.first()
                assertThat(calendarPrefs.observeLegendCollapsed().first()).isFalse()
                viewModel.selectJalaliDate(JalaliDate(1403, 12, 11))
                idle()
                assertThat(viewModel.uiState.value.legendExpanded).isTrue()
                assertThat(calendarPrefs.observeLegendAutoCollapsedOnce().first()).isTrue()
                assertThat(calendarPrefs.observeLegendCollapsed().first()).isFalse()
            }
        }

    @Test
    fun newViewModelInstance_keepsLegendExpandedAfterUserReExpand() =
        runTest(testDispatcher) {
            withLegendHarness { _, calendarPrefs, repository, viewModelStore ->
                val first =
                    viewModelStore.hold(
                        CalendarViewModel(
                            repository = repository,
                            calendarUiPreferencesRepository = calendarPrefs,
                            localDateClock = FixedLocalDateClock(LocalDate.of(2025, 3, 6)),
                        ),
                    )
                idle()
                first.selectJalaliDate(JalaliDate(1403, 12, 10))
                idle()
                first.uiState.filter { !it.legendExpanded && it.legendAutoCollapsedOnce }.first()
                first.toggleLegendExpanded()
                idle()
                first.uiState.filter { it.legendExpanded }.first()
                viewModelStore.clear()
                idle()
                val second =
                    viewModelStore.hold(
                        CalendarViewModel(
                            repository = repository,
                            calendarUiPreferencesRepository = calendarPrefs,
                            localDateClock = FixedLocalDateClock(LocalDate.of(2025, 3, 6)),
                        ),
                    )
                idle()
                second.uiState.filter { it.legendExpanded && it.legendAutoCollapsedOnce }.first()
            }
        }

    private suspend fun <T> withLegendHarness(
        block: suspend (
            CalendarViewModel,
            CalendarUiPreferencesRepository,
            CycleTrackingRepository,
            ViewModelStoreTestHarness,
        ) -> T,
    ): T {
        val calendarPrefs = CalendarUiPreferencesRepository(context)
        calendarPrefs.clear()
        val database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        database.openHelper.writableDatabase
        val cycleRepository = CycleTrackingRepository(database)
        seedProfile(database)
        val viewModelStore = ViewModelStoreTestHarness()
        val viewModel =
            viewModelStore.hold(
                CalendarViewModel(
                    repository = cycleRepository,
                    calendarUiPreferencesRepository = calendarPrefs,
                    localDateClock = FixedLocalDateClock(LocalDate.of(2025, 3, 6)),
                ),
            )
        try {
            return block(viewModel, calendarPrefs, cycleRepository, viewModelStore)
        } finally {
            testDispatcher.scheduler.advanceUntilIdle()
            ShadowLooper.idleMainLooper()
            viewModelStore.clear()
            calendarPrefs.clear()
            database.close()
        }
    }

    private suspend fun seedProfile(database: MahinDatabase) {
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
