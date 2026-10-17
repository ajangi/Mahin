package dev.mahin.core.content

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.datastore.ContentCachePreferencesRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ContentRepositoryPregnancyWeekFetchTest {
    private val api = RecordingContentApi()
    private lateinit var repository: ContentRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository =
            ContentRepository(
                api = api,
                cachePreferences = ContentCachePreferencesRepository(context),
                localStore =
                    WellnessContentLocalStore(
                        database.wellnessContentDao(),
                        Json { ignoreUnknownKeys = true },
                    ),
                json = Json { ignoreUnknownKeys = true },
                mediaMemoryCache = MediaAssetMemoryCache(),
            )
    }

    @Test
    fun pregnancyWeekFetch_notPublished_returnsLoadedWithNullArticle() {
        runBlocking {
            api.pregnancyWeekFailure =
                HttpException(Response.error<ContentArticleDto>(404, "".toResponseBody(null)))
            val missing = repository.pregnancyWeekFetch(8)
            assertThat(missing).isInstanceOf(ContentRepository.PregnancyWeekFetch.Loaded::class.java)
            assertThat((missing as ContentRepository.PregnancyWeekFetch.Loaded).article).isNull()
        }
    }

    @Test
    fun pregnancyWeekFetch_networkFailure_isDistinctFromLoaded() {
        runBlocking {
            api.pregnancyWeekFailure = IllegalStateException("offline")
            assertThat(repository.pregnancyWeekFetch(8))
                .isInstanceOf(ContentRepository.PregnancyWeekFetch.NetworkError::class.java)
            api.pregnancyWeekFailure = null
            api.pregnancyWeekArticle =
                ContentArticleDto(
                    id = "w8",
                    slug = "week-8",
                    locale = "fa-IR",
                    title = "هفته ۸",
                    summary = null,
                    body = null,
                    status = "published",
                    gestationalWeek = 8,
                )
            val loaded = repository.pregnancyWeekFetch(8)
            assertThat(loaded).isInstanceOf(ContentRepository.PregnancyWeekFetch.Loaded::class.java)
            assertThat((loaded as ContentRepository.PregnancyWeekFetch.Loaded).article?.title).isEqualTo("هفته ۸")
        }
    }

    private class RecordingContentApi : ContentApi {
        var pregnancyWeekFailure: Throwable? = null
        var pregnancyWeekArticle: ContentArticleDto? = null

        override suspend fun catalogStatus(): ContentCatalogStatusDto =
            ContentCatalogStatusDto(publicationRevision = 0L, updatedAt = "2025-01-01T00:00:00Z")

        override suspend fun pregnancyWeek(
            week: Int,
            locale: String,
        ): ContentArticleDto {
            pregnancyWeekFailure?.let { throw it }
            return pregnancyWeekArticle ?: error("missing")
        }

        override suspend fun search(
            query: String?,
            locale: String?,
            lifeStage: String?,
        ): ContentSearchResponseDto = error("unused")

        override suspend fun article(articleId: String): ContentArticleDto = error("unused")

        override suspend fun dailyTips(
            locale: String,
            mode: String?,
            cyclePhase: String?,
            pregnancyWeek: Int?,
        ): ContentDailyTipListResponseDto = ContentDailyTipListResponseDto(emptyList())

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
