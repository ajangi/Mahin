package dev.mahin.core.content

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.datastore.ContentCachePreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ContentRepositoryDailyTipTest {
    private lateinit var context: Context
    private lateinit var database: MahinDatabase
    private lateinit var cachePreferences: ContentCachePreferencesRepository
    private val json = Json { ignoreUnknownKeys = true }
    private val api = RecordingContentApi()
    private lateinit var repository: ContentRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        cachePreferences = ContentCachePreferencesRepository(context)
        repository =
            ContentRepository(
                api = api,
                cachePreferences = cachePreferences,
                localStore = WellnessContentLocalStore(database.wellnessContentDao(), json),
                json = json,
                mediaMemoryCache = MediaAssetMemoryCache(),
                ioDispatcher = Dispatchers.IO,
            )
    }

    @After
    fun tearDown() {
        runBlocking { cachePreferences.clearCachedDailyTip() }
        database.close()
    }

    @Test
    fun fetchDailyTips_successEmpty_isNotNetworkFailure() {
        runBlocking {
            api.tips = emptyList()
            val key = dailyKey()
            val result = repository.fetchDailyTips(key)
            assertThat(result).isInstanceOf(DailyTipFetchResult.Success::class.java)
            assertThat((result as DailyTipFetchResult.Success).tips).isEmpty()
        }
    }

    @Test
    fun fetchDailyTips_networkFailure_isDistinctFromSuccessEmpty() {
        runBlocking {
            api.throwOnDailyTips = true
            val result = repository.fetchDailyTips(dailyKey())
            assertThat(result).isInstanceOf(DailyTipFetchResult.NetworkFailure::class.java)
        }
    }

    private fun dailyKey() =
        DailyTipTargetingKey(
            mode = "cycle",
            cyclePhase = null,
            pregnancyWeek = null,
            localDateIso = "2025-06-15",
        )

    private class RecordingContentApi : ContentApi {
        var tips: List<ContentDailyTipDto> = emptyList()
        var throwOnDailyTips: Boolean = false

        override suspend fun catalogStatus(): ContentCatalogStatusDto =
            ContentCatalogStatusDto(publicationRevision = 0L, updatedAt = "2025-01-01T00:00:00Z")

        override suspend fun dailyTips(
            locale: String,
            mode: String?,
            cyclePhase: String?,
            pregnancyWeek: Int?,
        ): ContentDailyTipListResponseDto {
            if (throwOnDailyTips) error("offline")
            return ContentDailyTipListResponseDto(tips)
        }

        override suspend fun search(
            query: String?,
            locale: String?,
            lifeStage: String?,
        ): ContentSearchResponseDto = error("unused")

        override suspend fun article(articleId: String): ContentArticleDto = error("unused")

        override suspend fun pregnancyWeek(
            week: Int,
            locale: String,
        ): ContentArticleDto = error("unused")

        override suspend fun programs(
            locale: String,
            programType: String?,
        ): ContentProgramListResponseDto = error("unused")

        override suspend fun program(documentId: String): ContentProgramDto = error("unused")

        override suspend fun collections(
            locale: String,
            category: String?,
        ): ContentCollectionListResponseDto = error("unused")

        override suspend fun collection(documentId: String): ContentCollectionDto = error("unused")

        override suspend fun mediaAsset(assetId: String): MediaAssetDto = error("unused")

        override suspend fun careSchedules(
            locale: String,
            gestationalWeek: Int?,
        ): ContentCareScheduleListResponseDto = ContentCareScheduleListResponseDto(emptyList())

        override suspend fun careSchedule(documentId: String): ContentCareScheduleDto = error("unused")

        override suspend fun checklistTemplates(
            locale: String,
            checklistKind: String?,
        ): ContentChecklistTemplateListResponseDto = ContentChecklistTemplateListResponseDto(emptyList())

        override suspend fun checklistTemplate(documentId: String): ContentChecklistTemplateDto = error("unused")

        override suspend fun warningSigns(locale: String): ContentWarningSignsListResponseDto =
            ContentWarningSignsListResponseDto(emptyList())

        override suspend fun warningSignsDetail(documentId: String): ContentWarningSignsDto = error("unused")
    }
}
