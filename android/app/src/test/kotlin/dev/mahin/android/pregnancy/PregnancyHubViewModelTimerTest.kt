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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
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
    val mainDispatcherRule =
        MainDispatcherRule(
            testDispatcher,
            beforeResetMain = { viewModelStore.clear() },
        )

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
        instantClock = FixedInstantClock(Instant.ofEpochMilli(1_000_000L))
        repository = PregnancyTrackingRepository(database, timerPreferences, instantClock)
        contentRepository =
            ContentRepository(
                FakeContentApi(),
                ContentCachePreferencesRepository(context),
                WellnessContentLocalStore(database.wellnessContentDao(), Json { ignoreUnknownKeys = true }),
                Json { ignoreUnknownKeys = true },
                MediaAssetMemoryCache(),
            )
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
            try {
                runCurrent()
                vm.uiState.filter { !it.isLoading }.first()
                vm.startKickSession()
                runCurrent()
                vm.uiState.filter { it.kickSessionId != null }.first()
                val kickStart =
                    timerPreferences.observeKickTimer().first().startedAtEpochMs ?: error("missing kick start")
                instantClock.set(Instant.ofEpochMilli(kickStart + 1_000))
                advanceTimeBy(1_000)
                runCurrent()
                assertThat(vm.uiState.value.kickElapsedSeconds).isEqualTo(1L)
                instantClock.set(Instant.ofEpochMilli(kickStart + 2_000))
                advanceTimeBy(1_000)
                runCurrent()
                assertThat(vm.uiState.value.kickElapsedSeconds).isEqualTo(2L)
            } finally {
                viewModelStore.clear()
            }
        }

    @Test
    fun kickTimer_survivesViewModelRecreate() =
        runTest(testDispatcher) {
            var sessionId: String? = null
            val vm1 = createViewModel()
            try {
                runCurrent()
                vm1.uiState.filter { !it.isLoading }.first()
                vm1.startKickSession()
                runCurrent()
                vm1.uiState.filter { it.kickSessionId != null }.first()
                val kickStart =
                    timerPreferences.observeKickTimer().first().startedAtEpochMs ?: error("missing kick start")
                sessionId = vm1.uiState.value.kickSessionId
                instantClock.set(Instant.ofEpochMilli(kickStart + 6_000))
            } finally {
                viewModelStore.clear()
            }
            val vm2 = createViewModel()
            try {
                runCurrent()
                vm2.uiState.filter { it.kickSessionId == sessionId }.first()
                advanceTimeBy(1_000)
                runCurrent()
                assertThat(vm2.uiState.value.kickElapsedSeconds).isEqualTo(6L)
            } finally {
                viewModelStore.clear()
            }
        }

    @Test
    fun contractionTimer_advancesWhileEventOpen() =
        runTest(testDispatcher) {
            val vm = createViewModel()
            try {
                runCurrent()
                vm.uiState.filter { !it.isLoading }.first()
                vm.startContractionSession()
                runCurrent()
                vm.uiState.filter { it.contractionSessionId != null }.first()
                vm.toggleContraction()
                runCurrent()
                vm.uiState.filter { it.openContractionEventId != null }.first()
                val openStart =
                    timerPreferences.observeContractionTimer().first().openEventStartedEpochMs
                        ?: error("missing contraction start")
                instantClock.set(Instant.ofEpochMilli(openStart + 1_000))
                advanceTimeBy(1_000)
                runCurrent()
                assertThat(vm.uiState.value.contractionElapsedSeconds).isEqualTo(1L)
                instantClock.set(Instant.ofEpochMilli(openStart + 2_000))
                advanceTimeBy(1_000)
                runCurrent()
                assertThat(vm.uiState.value.contractionElapsedSeconds).isEqualTo(2L)
            } finally {
                viewModelStore.clear()
            }
        }

    @Test
    fun contractionTimer_survivesViewModelRecreate() =
        runTest(testDispatcher) {
            val vm1 = createViewModel()
            val sessionId: String?
            val openEventId: String?
            try {
                runCurrent()
                vm1.uiState.filter { !it.isLoading }.first()
                vm1.startContractionSession()
                runCurrent()
                vm1.uiState.filter { it.contractionSessionId != null }.first()
                vm1.toggleContraction()
                runCurrent()
                vm1.uiState.filter { it.openContractionEventId != null }.first()
                val openStart =
                    timerPreferences.observeContractionTimer().first().openEventStartedEpochMs
                        ?: error("missing contraction start")
                sessionId = vm1.uiState.value.contractionSessionId
                openEventId = vm1.uiState.value.openContractionEventId
                instantClock.set(Instant.ofEpochMilli(openStart + 5_000))
            } finally {
                viewModelStore.clear()
            }
            val vm2 = createViewModel()
            try {
                runCurrent()
                vm2.uiState.filter { it.contractionSessionId == sessionId }.first()
                advanceTimeBy(1_000)
                runCurrent()
                assertThat(vm2.uiState.value.openContractionEventId).isEqualTo(openEventId)
                assertThat(vm2.uiState.value.contractionElapsedSeconds).isEqualTo(5L)
            } finally {
                viewModelStore.clear()
            }
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
