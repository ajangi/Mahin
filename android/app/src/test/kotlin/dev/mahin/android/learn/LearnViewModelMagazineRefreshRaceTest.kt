package dev.mahin.android.learn

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.common.FixedLocalDateClock
import dev.mahin.core.content.ContentArticleSummaryDto
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
import dev.mahin.core.testing.buildMahinInMemoryDatabase
import java.time.LocalDate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.robolectric.shadows.ShadowLooper

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LearnViewModelMagazineRefreshRaceTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule =
        MainDispatcherRule(
            testDispatcher,
            beforeResetMain = {
                testDispatcher.scheduler.advanceUntilIdle()
                viewModelStore.clear()
                ShadowLooper.idleMainLooper()
            },
        )

    private lateinit var harness: LearnContentTestHarness
    private val viewModelStore = ViewModelStoreTestHarness()
    private lateinit var database: MahinDatabase
    private lateinit var cycleRepository: CycleTrackingRepository
    private lateinit var pregnancyRepository: PregnancyTrackingRepository
    private val localDateClock = FixedLocalDateClock(LocalDate.of(2025, 6, 15))

    private val staleCycleArticle =
        ContentArticleSummaryDto(
            id = "cycle-stale",
            slug = "cycle-stale",
            locale = "fa-IR",
            title = "چرخه",
            contentType = "article",
        )
    private val ttcArticle =
        ContentArticleSummaryDto(
            id = "ttc-current",
            slug = "ttc-current",
            locale = "fa-IR",
            title = "باردار شدن",
            contentType = "article",
        )

    @Before
    fun setUp() {
        viewModelStore.clear()
        harness =
            LearnContentTestHarness(
                ioDispatcher = testDispatcher,
                viewModelStore = viewModelStore,
            )
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
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = buildMahinInMemoryDatabase(context)
        cycleRepository = CycleTrackingRepository(database)
        pregnancyRepository = PregnancyTrackingRepository(database, PregnancyTimerPreferencesRepository(context))
    }

    @After
    fun tearDown() {
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()
        harness.close()
        database.close()
    }

    @Test
    fun staleMagazineRefresh_doesNotOverwriteLatestLifeStageSearch() =
        runTest(testDispatcher) {
            val cycleSearchGate = CompletableDeferred<Unit>()
            val cycleSearchEntered = CompletableDeferred<Unit>()
            val cycleSearchFinished = CompletableDeferred<Unit>()
            harness.api.beforeSearch = { lifeStage ->
                when (lifeStage) {
                    "cycle" -> {
                        cycleSearchEntered.complete(Unit)
                        harness.api.searchResults = listOf(staleCycleArticle)
                        try {
                            cycleSearchGate.await()
                        } finally {
                            cycleSearchFinished.complete(Unit)
                        }
                    }
                    "ttc" -> harness.api.searchResults = listOf(ttcArticle)
                }
            }
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
            advanceUntilIdle()
            ShadowLooper.idleMainLooper()
            advanceUntilIdle()
            cycleSearchEntered.await()
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
            advanceUntilIdle()
            ShadowLooper.idleMainLooper()
            advanceUntilIdle()
            viewModel.uiState
                .filter { state ->
                    !state.loading &&
                        state.contextualArticles.any { it.id == ttcArticle.id }
                }.first()
            cycleSearchGate.complete(Unit)
            advanceUntilIdle()
            cycleSearchFinished.await()
            advanceUntilIdle()
            val finalState = viewModel.uiState.value
            assertThat(harness.api.lastSearchLifeStage).isEqualTo("ttc")
            assertThat(finalState.loading).isFalse()
            assertThat(finalState.contextualArticles.map { it.id }).containsExactly(ttcArticle.id)
            assertThat(finalState.contextualArticles.map { it.id }).doesNotContain(staleCycleArticle.id)
        }
}
