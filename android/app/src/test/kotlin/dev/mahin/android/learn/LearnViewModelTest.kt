package dev.mahin.android.learn

import android.content.Context
import androidx.room.Room
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
import dev.mahin.core.testing.ViewModelStoreTestHarness
import dev.mahin.domain.pregnancy.PregnancyDatingEngineV1
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
class LearnViewModelTest {
    private val harness = LearnContentTestHarness()
    private val viewModelStore = ViewModelStoreTestHarness()
    private lateinit var database: MahinDatabase
    private lateinit var cycleRepository: CycleTrackingRepository
    private lateinit var pregnancyRepository: PregnancyTrackingRepository
    private val localDateClock = FixedLocalDateClock(LocalDate.of(2025, 6, 15))

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
        viewModelStore.clear()
        harness.close()
        database.close()
    }

    @Test
    fun search_normalizesPersianQueryBeforeRepositoryCall() {
        runBlocking {
            seedCycleProfile()
            harness.api.searchResults =
                listOf(
                    ContentArticleSummaryDto(
                        id = "a1",
                        slug = "a",
                        locale = "fa-IR",
                        title = "نتیجه",
                        contentType = "article",
                    ),
                )
            val viewModel = createViewModel()
            viewModel.onQueryChange("علائم يك")
            viewModel.search()
            awaitUntil { !viewModel.uiState.value.loading }
            assertThat(harness.api.lastSearchQuery).isEqualTo("علائم یک")
            assertThat(viewModel.uiState.value.searchResults).hasSize(1)
        }
    }

    @Test
    fun refreshMagazine_usesTtcLifeStageWhenProfileIsTtc() {
        runBlocking {
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
            val viewModel = createViewModel()
            awaitUntil {
                viewModel.uiState.value.featuredCollections
                    .isNotEmpty()
            }
            awaitUntil { harness.api.lastSearchLifeStage == "ttc" }
        }
    }

    @Test
    fun refreshMagazine_filtersPregnancyWeekWhenPregnant() {
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
            val pregnancy =
                pregnancyRepository.startPregnancy(
                    lmpDate = LocalDate.of(2025, 3, 1),
                    clinicalEddDate = null,
                    datingReason = null,
                )
            val week =
                PregnancyDatingEngineV1
                    .status(
                        lmpDate = pregnancy.lmpDate,
                        clinicalEddDate = pregnancy.clinicalEddDate,
                        asOfDate = localDateClock.today(),
                    ).displayWeekNumber
            harness.api.searchResults =
                listOf(
                    ContentArticleSummaryDto(
                        id = "w-other",
                        slug = "w-other",
                        locale = "fa-IR",
                        title = "هفته دیگر",
                        contentType = "article",
                        gestationalWeek = week + 3,
                    ),
                    ContentArticleSummaryDto(
                        id = "w-match",
                        slug = "w-match",
                        locale = "fa-IR",
                        title = "هفته جاری",
                        contentType = "article",
                        gestationalWeek = week,
                    ),
                )
            val viewModel = createViewModel()
            awaitUntil {
                viewModel.uiState.value.contextualArticles
                    .isNotEmpty()
            }
            assertThat(harness.api.lastSearchLifeStage).isEqualTo("pregnancy")
            assertThat(
                viewModel.uiState.value.contextualArticles
                    .map { it.id },
            ).contains("w-match")
            assertThat(
                viewModel.uiState.value.contextualArticles
                    .map { it.id },
            ).doesNotContain("w-other")
        }
    }

    @Test
    fun toggleBookmark_persistsLocally() {
        runBlocking {
            seedCycleProfile()
            val viewModel = createViewModel()
            viewModel.toggleBookmark("article-1")
            awaitUntil {
                viewModel.uiState.value.bookmarkedIds
                    .contains("article-1")
            }
            assertThat(harness.cachePreferences.localBookmarkIds()).contains("article-1")
        }
    }

    private fun createViewModel(): LearnViewModel =
        viewModelStore.hold(
            LearnViewModel(
                contentRepository = harness.repository,
                cachePreferences = harness.cachePreferences,
                cycleRepository = cycleRepository,
                pregnancyRepository = pregnancyRepository,
                localDateClock = localDateClock,
            ),
        )

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
