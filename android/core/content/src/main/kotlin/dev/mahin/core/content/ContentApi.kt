package dev.mahin.core.content

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ContentApi {
    @GET("v1/content/catalog-status")
    suspend fun catalogStatus(): ContentCatalogStatusDto

    @GET("v1/content/search")
    suspend fun search(
        @Query("q") query: String?,
        @Query("locale") locale: String? = "fa-IR",
        @Query("lifeStage") lifeStage: String? = null,
    ): ContentSearchResponseDto

    @GET("v1/content/articles/{articleId}")
    suspend fun article(
        @Path("articleId") articleId: String,
    ): ContentArticleDto

    @GET("v1/content/pregnancy/weeks/{week}")
    suspend fun pregnancyWeek(
        @Path("week") week: Int,
        @Query("locale") locale: String = "fa-IR",
    ): ContentArticleDto

    @GET("v1/content/daily-tips")
    suspend fun dailyTips(
        @Query("locale") locale: String = "fa-IR",
        @Query("mode") mode: String? = null,
        @Query("cyclePhase") cyclePhase: String? = null,
        @Query("pregnancyWeek") pregnancyWeek: Int? = null,
    ): ContentDailyTipListResponseDto

    @GET("v1/content/programs")
    suspend fun programs(
        @Query("locale") locale: String = "fa-IR",
        @Query("programType") programType: String? = null,
    ): ContentProgramListResponseDto

    @GET("v1/content/programs/{documentId}")
    suspend fun program(
        @Path("documentId") documentId: String,
    ): ContentProgramDto

    @GET("v1/content/collections")
    suspend fun collections(
        @Query("locale") locale: String = "fa-IR",
        @Query("category") category: String? = null,
    ): ContentCollectionListResponseDto

    @GET("v1/content/collections/{documentId}")
    suspend fun collection(
        @Path("documentId") documentId: String,
    ): ContentCollectionDto

    @GET("v1/media/assets/{assetId}")
    suspend fun mediaAsset(
        @Path("assetId") assetId: String,
    ): MediaAssetDto

    @GET("v1/content/care-schedules")
    suspend fun careSchedules(
        @Query("locale") locale: String = "fa-IR",
        @Query("gestationalWeek") gestationalWeek: Int? = null,
    ): ContentCareScheduleListResponseDto

    @GET("v1/content/care-schedules/{documentId}")
    suspend fun careSchedule(
        @Path("documentId") documentId: String,
    ): ContentCareScheduleDto

    @GET("v1/content/checklist-templates")
    suspend fun checklistTemplates(
        @Query("locale") locale: String = "fa-IR",
        @Query("checklistKind") checklistKind: String? = "hospital_bag",
    ): ContentChecklistTemplateListResponseDto

    @GET("v1/content/checklist-templates/{documentId}")
    suspend fun checklistTemplate(
        @Path("documentId") documentId: String,
    ): ContentChecklistTemplateDto
}

@Serializable
data class ContentCatalogStatusDto(
    val publicationRevision: Long,
    val updatedAt: String,
)

@Serializable
data class ContentSearchResponseDto(
    val items: List<ContentArticleSummaryDto>,
)

@Serializable
data class ContentArticleSummaryDto(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String? = null,
    val lifeStage: String? = null,
    val contentType: String,
    val gestationalWeek: Int? = null,
)

@Serializable
data class ContentArticleDto(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String? = null,
    val body: String? = null,
    val contentType: String = "article",
    val status: String,
    val withdrawn: Boolean = false,
    val medicalRiskLevel: String? = null,
    val contentVersion: Int? = null,
    val sourceReferences: List<String> = emptyList(),
    val clinicalReviewer: String? = null,
    val clinicalReviewedAt: String? = null,
    val gestationalWeek: Int? = null,
    val placeholder: Boolean = false,
    val fetalSummary: String? = null,
    val maternalSummary: String? = null,
    val sizeComparisonLabel: String? = null,
    val illustrationMediaAssetId: String? = null,
)
