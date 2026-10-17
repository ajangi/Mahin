package dev.mahin.core.content

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.datastore.ContentCachePreferencesRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ContentRepositoryWarningSignsCacheTest {
    private val api = RecordingWarningSignsApi()
    private lateinit var cachePreferences: ContentCachePreferencesRepository
    private lateinit var repository: ContentRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        cachePreferences = ContentCachePreferencesRepository(context)
        val database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository =
            ContentRepository(
                api = api,
                cachePreferences = cachePreferences,
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
    fun warningSignsFetch_emptyList_clearsStaleCache() {
        runBlocking {
            val staleCard =
                ContentWarningSignsDto(
                    id = "old",
                    slug = "old",
                    locale = "fa-IR",
                    title = "قدیمی",
                    introTitle = "i",
                    introBody = "b",
                    signs = emptyList(),
                    bpThresholds =
                        ContentPregnancyBpThresholdsDto(
                            elevatedSystolic = 140,
                            elevatedDiastolic = 90,
                            urgentSystolic = 160,
                            urgentDiastolic = 110,
                        ),
                    escalationTitle = "e",
                    escalationBody = "e",
                    urgentEscalationTitle = "u",
                    urgentEscalationBody = "u",
                    contactClinicianLabel = "c",
                    urgentContactClinicianLabel = "u",
                )
            cachePreferences.setCachedWarningSignsCardJson(
                Json.encodeToString(ContentWarningSignsDto.serializer(), staleCard),
            )
            api.warningSignsItems = emptyList()
            assertThat(repository.warningSignsCardFetch()).isNull()
            assertThat(cachePreferences.cachedWarningSignsCardJson()).isNull()
        }
    }

    private class RecordingWarningSignsApi : ContentApi {
        var warningSignsItems: List<ContentWarningSignsSummaryDto> = emptyList()

        override suspend fun catalogStatus(): ContentCatalogStatusDto =
            ContentCatalogStatusDto(publicationRevision = 0L, updatedAt = "2025-01-01T00:00:00Z")

        override suspend fun warningSigns(locale: String): ContentWarningSignsListResponseDto =
            ContentWarningSignsListResponseDto(items = warningSignsItems)

        override suspend fun warningSignsDetail(documentId: String): ContentWarningSignsDto = error("unused")

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

        override suspend fun dailyTips(
            locale: String,
            mode: String?,
            cyclePhase: String?,
            pregnancyWeek: Int?,
        ): ContentDailyTipListResponseDto = ContentDailyTipListResponseDto(emptyList())

        override suspend fun programs(
            locale: String,
            programType: String?,
        ): ContentProgramListResponseDto = ContentProgramListResponseDto(emptyList())

        override suspend fun program(documentId: String): ContentProgramDto = error("unused")

        override suspend fun collections(
            locale: String,
            category: String?,
        ): ContentCollectionListResponseDto = ContentCollectionListResponseDto(emptyList())

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
    }
}
