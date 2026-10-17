package dev.mahin.android.pregnancy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.android.learn.RecordingAnalyticsGateway
import dev.mahin.core.analytics.GuardedAnalyticsGateway
import dev.mahin.core.common.FixedLocalDateClock
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.pregnancy.PregnancyDayLogInput
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PregnancyWeightChartViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var database: MahinDatabase
    private lateinit var repository: PregnancyTrackingRepository
    private val viewModelStore = ViewModelStoreTestHarness()
    private val today = LocalDate.of(2025, 6, 15)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository = PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
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
        }
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        database.close()
    }

    @Test
    fun noActivePregnancy_emptyState() =
        runTest(testDispatcher) {
            val vm = createViewModel()
            val state = vm.uiState.filter { !it.isLoading }.first()
            assertThat(state.hasActivePregnancy).isFalse()
            assertThat(state.points).isEmpty()
        }

    @Test
    fun activePregnancy_noWeights_emptyPoints() =
        runTest(testDispatcher) {
            repository.startPregnancy(today.minusWeeks(12), null, null)
            val vm = createViewModel()
            val state = vm.uiState.filter { it.hasActivePregnancy }.first()
            assertThat(state.points).isEmpty()
        }

    @Test
    fun missingWeightOnLog_excludedFromChart() =
        runTest(testDispatcher) {
            repository.startPregnancy(today.minusWeeks(12), null, null)
            repository.upsertPregnancyDayLog(
                PregnancyDayLogInput(today, emptySet(), null, 120, 80, null),
            )
            val vm = createViewModel()
            val state = vm.uiState.filter { it.hasActivePregnancy }.first()
            assertThat(state.points).isEmpty()
        }

    @Test
    fun extremeWeightValues_chartIncludesPoint() =
        runTest(testDispatcher) {
            repository.startPregnancy(today.minusWeeks(12), null, null)
            repository.upsertPregnancyDayLog(
                PregnancyDayLogInput(today, emptySet(), 250.0, null, null, null),
            )
            val vm = createViewModel()
            val state = vm.uiState.filter { it.points.isNotEmpty() }.first()
            assertThat(state.points.first().weightKg).isEqualTo(250.0)
        }

    @Test
    fun logOutsideCurrentPregnancy_excluded() =
        runTest(testDispatcher) {
            repository.startPregnancy(today.minusWeeks(8), null, null)
            repository.upsertPregnancyDayLog(
                PregnancyDayLogInput(today.minusYears(2), emptySet(), 60.0, null, null, null),
            )
            val vm = createViewModel()
            val state = vm.uiState.filter { it.hasActivePregnancy }.first()
            assertThat(state.points).isEmpty()
        }

    private fun createViewModel(): PregnancyWeightChartViewModel =
        viewModelStore.hold(
            PregnancyWeightChartViewModel(
                trackingRepository = repository,
                localDateClock = FixedLocalDateClock(today),
                analytics = GuardedAnalyticsGateway(RecordingAnalyticsGateway()),
            ),
        )
}
