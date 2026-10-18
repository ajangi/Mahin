package dev.mahin.android.learn

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.common.FixedLocalDateClock
import dev.mahin.core.content.ContentCollectionSummaryDto
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.MainDispatcherRule
import dev.mahin.core.testing.ViewModelStoreTestHarness
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LearnViewModelMagazineRefreshRaceTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule =
        MainDispatcherRule(
            testDispatcher,
            beforeResetMain = { viewModelStore.clear() },
        )

    private lateinit var harness: LearnContentTestHarness
    private val viewModelStore = ViewModelStoreTestHarness()
    private lateinit var database: MahinDatabase
    private lateinit var cycleRepository: CycleTrackingRepository
    private lateinit var pregnancyRepository: PregnancyTrackingRepository
    private val localDateClock = FixedLocalDateClock(LocalDate.of(2025, 6, 15))

    @Before
    fun setUp() {
        viewModelStore.clear()
        harness = LearnContentTestHarness()
        harness.api.searchResults = emptyList()
        harness.api.collections =
            listOf(
                ContentCollectionSummaryDto(
                    id = "c1",
                    slug = "c",
                    locale = "fa-IR",
                    title = "مجموعه",
                    category = "cycle",
                    featured = true,
                ),
            )
        harness.api.beforeSearch = { lifeStage ->
            if (lifeStage == "cycle") {
                delay(1)
            }
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        database.openHelper.writableDatabase
        cycleRepository = CycleTrackingRepository(database)
        pregnancyRepository = PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        harness.close()
        database.close()
    }

    @Test
    fun staleMagazineRefresh_doesNotOverwriteLatestLifeStageSearch() =
        runTest(testDispatcher) {
            database.cycleProfileDao().upsert(
                CycleProfileEntity(
                    reproductiveMode = ReproductiveMode.TRYING_TO_CONCEIVE,
                    typicalCycleLengthDays = 28,
                    typicalPeriodLengthDays = 5,
                    regularity = CycleRegularity.REGULAR,
                    onboardingCompleted = true,
                    updatedAtEpochMs = 0L,
                ),
            )
            val viewModel =
                viewModelStore.hold(
                    LearnViewModel(
                        contentRepository = harness.repository,
                        cachePreferences = harness.cachePreferences,
                        cycleRepository = cycleRepository,
                        pregnancyRepository = pregnancyRepository,
                        localDateClock = localDateClock,
                    ),
                )
            viewModel.uiState.filter { it.featuredCollections.isNotEmpty() }.first()
            advanceUntilIdle()
            assertThat(harness.api.lastSearchLifeStage).isEqualTo("ttc")
        }
}
