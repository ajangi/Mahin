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

    @GET("v1/content/pregnancy/weeks/{week}")
    suspend fun pregnancyWeek(
        @Path("week") week: Int,
        @Query("locale") locale: String = "fa-IR",
    ): ContentArticleDto
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
    val contentType: String,
    val status: String,
    val withdrawn: Boolean = false,
)
