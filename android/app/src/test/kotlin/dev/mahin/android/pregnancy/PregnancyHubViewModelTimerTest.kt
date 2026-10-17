package dev.mahin.android.pregnancy

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.android.learn.FakeContentApi
import dev.mahin.core.common.FixedInstantClock
import dev.mahin.core.common.FixedLocalDateClock
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.content.MediaAssetMemoryCache
import dev.mahin.core.content.WellnessContentLocalStore
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.ContentCachePreferencesRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.MainDispatcherRule
import dev.mahin.core.testing.ViewModelStoreTestHarness
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PregnancyHubViewModelTimerTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    private lateinit var database: MahinDatabase
    private lateinit var repository: PregnancyTrackingRepository
    private lateinit var timerPreferences: PregnancyTimerPreferencesRepository
    private lateinit var contentRepository: ContentRepository
    private lateinit var instantClock: FixedInstantClock
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
        timerPreferences = PregnancyTimerPreferencesRepository(context)
        repository = PregnancyTrackingRepository(database, timerPreferences)
        contentRepository =
            ContentRepository(
                FakeContentApi(),
                ContentCachePreferencesRepository(context),
                WellnessContentLocalStore(database.wellnessContentDao(), Json { ignoreUnknownKeys = true }),
                Json { ignoreUnknownKeys = true },
                MediaAssetMemoryCache(),
            )
        instantClock = FixedInstantClock(Instant.ofEpochMilli(1_000_000L))
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
            repository.startPregnancy(today.minusWeeks(20), null, null)
        }
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        database.close()
    }

    @Test
    fun kickTimer_advancesElapsedWithFakeClock() =
        runTest(testDispatcher) {
            val vm = createViewModel()
            advanceUntilIdle()
            vm.startKickSession()
            advanceUntilIdle()
            val kickStart =
                timerPreferences.observeKickTimer().first().startedAtEpochMs ?: error("missing kick start")
            instantClock.set(Instant.ofEpochMilli(kickStart + 4_500))
            advanceTimeBy(1_000)
            advanceUntilIdle()
            assertThat(vm.uiState.value.kickElapsedSeconds).isAtLeast(4L)
            assertThat(vm.uiState.value.kickSessionId).isNotNull()
        }

    @Test
    fun kickTimer_survivesViewModelRecreate() =
        runTest(testDispatcher) {
            val vm1 = createViewModel()
            advanceUntilIdle()
            vm1.startKickSession()
            advanceUntilIdle()
            val kickStart =
                timerPreferences.observeKickTimer().first().startedAtEpochMs ?: error("missing kick start")
            val sessionId = vm1.uiState.value.kickSessionId
            instantClock.set(Instant.ofEpochMilli(kickStart + 6_000))
            advanceTimeBy(1_000)
            advanceUntilIdle()
            viewModelStore.clear()
            val vm2 = createViewModel()
            advanceUntilIdle()
            advanceTimeBy(1_000)
            advanceUntilIdle()
            assertThat(vm2.uiState.value.kickSessionId).isEqualTo(sessionId)
            assertThat(vm2.uiState.value.kickElapsedSeconds).isAtLeast(6L)
        }

    @Test
    fun contractionTimer_advancesWhileEventOpen() =
        runTest(testDispatcher) {
            val vm = createViewModel()
            advanceUntilIdle()
            vm.startContractionSession()
            advanceUntilIdle()
            vm.toggleContraction()
            advanceUntilIdle()
            val openStart =
                timerPreferences.observeContractionTimer().first().openEventStartedEpochMs
                    ?: error("missing contraction start")
            instantClock.set(Instant.ofEpochMilli(openStart + 3_500))
            advanceTimeBy(1_000)
            advanceUntilIdle()
            assertThat(vm.uiState.value.contractionElapsedSeconds).isAtLeast(3L)
        }

    @Test
    fun contractionTimer_survivesViewModelRecreate() =
        runTest(testDispatcher) {
            val vm1 = createViewModel()
            advanceUntilIdle()
            vm1.startContractionSession()
            advanceUntilIdle()
            vm1.toggleContraction()
            advanceUntilIdle()
            val openStart =
                timerPreferences.observeContractionTimer().first().openEventStartedEpochMs
                    ?: error("missing contraction start")
            val sessionId = vm1.uiState.value.contractionSessionId
            val openEventId = vm1.uiState.value.openContractionEventId
            instantClock.set(Instant.ofEpochMilli(openStart + 5_000))
            advanceTimeBy(1_000)
            advanceUntilIdle()
            viewModelStore.clear()
            val vm2 = createViewModel()
            advanceUntilIdle()
            advanceTimeBy(1_000)
            advanceUntilIdle()
            assertThat(vm2.uiState.value.contractionSessionId).isEqualTo(sessionId)
            assertThat(vm2.uiState.value.openContractionEventId).isEqualTo(openEventId)
            assertThat(vm2.uiState.value.contractionElapsedSeconds).isAtLeast(5L)
        }

    private fun createViewModel(): PregnancyHubViewModel =
        viewModelStore.hold(
            PregnancyHubViewModel(
                repository = repository,
                timerPreferences = timerPreferences,
                contentRepository = contentRepository,
                localDateClock = FixedLocalDateClock(today),
                instantClock = instantClock,
                savedStateHandle = SavedStateHandle(),
            ),
        )
}
