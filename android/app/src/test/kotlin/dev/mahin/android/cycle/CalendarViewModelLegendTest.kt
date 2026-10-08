package dev.mahin.android.cycle

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.datastore.CalendarUiPreferencesRepository
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.ViewModelStoreTestHarness
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
class CalendarViewModelLegendTest {
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
    fun firstDateTap_autoCollapsesLegendAndSetsAutoCollapsedOnce() {
        runBlocking {
            val viewModel = createViewModel()
            awaitUntil { viewModel.uiState.value.legendExpanded }
            viewModel.selectJalaliDate(JalaliDate(1403, 12, 15))
            awaitUntil {
                val state = viewModel.uiState.value
                !state.legendExpanded && state.legendAutoCollapsedOnce
            }
        }
    }

    @Test
    fun toggleLegend_expandsAfterAutoCollapse() {
        runBlocking {
            val viewModel = createViewModel()
            viewModel.selectJalaliDate(JalaliDate(1403, 12, 10))
            awaitUntil { !viewModel.uiState.value.legendExpanded }
            viewModel.toggleLegendExpanded()
            awaitUntil { viewModel.uiState.value.legendExpanded }
        }
    }

    @Test
    fun secondDateTap_leavesLegendExpanded() {
        runBlocking {
            val viewModel = createViewModel()
            viewModel.selectJalaliDate(JalaliDate(1403, 12, 10))
            awaitUntil { !viewModel.uiState.value.legendExpanded }
            viewModel.toggleLegendExpanded()
            awaitUntil { viewModel.uiState.value.legendExpanded }
            viewModel.selectJalaliDate(JalaliDate(1403, 12, 11))
            awaitUntil { viewModel.uiState.value.legendExpanded }
            assertThat(viewModel.uiState.value.legendAutoCollapsedOnce).isTrue()
        }
    }

    @Test
    fun newViewModelInstance_keepsLegendExpandedAfterUserReExpand() {
        runBlocking {
            val first = createViewModel()
            first.selectJalaliDate(JalaliDate(1403, 12, 10))
            awaitUntil {
                val state = first.uiState.value
                !state.legendExpanded && state.legendAutoCollapsedOnce
            }
            first.toggleLegendExpanded()
            awaitUntil { first.uiState.value.legendExpanded }
            viewModelStore.clear()
            ShadowLooper.idleMainLooper()
            val second = createViewModel()
            awaitUntil(timeoutMs = 5_000) {
                val state = second.uiState.value
                state.legendExpanded && state.legendAutoCollapsedOnce
            }
        }
    }

    private fun createViewModel(): CalendarViewModel =
        viewModelStore.hold(
            CalendarViewModel(
                repository = cycleRepository,
                calendarUiPreferencesRepository = calendarPrefs,
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

    private suspend fun awaitUntil(
        timeoutMs: Long = 3_000,
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
