package dev.mahin.backend.content

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ContentArticleResponse(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String?,
    val body: String?,
    val contentType: String,
    val lifeStage: String?,
    val gestationalWeek: Int?,
    val tags: List<String>,
    val medicalRiskLevel: MedicalRiskLevel,
    val status: ContentStatus,
    val contentVersion: Int,
    val sourceReferences: List<String>,
    val clinicalReviewer: String?,
    val clinicalReviewedAt: String?,
    val nextReviewDueAt: String?,
    val effectiveFrom: String?,
    val effectiveTo: String?,
    val withdrawn: Boolean = false,
)

data class ContentCatalogStatusResponse(
    val publicationRevision: Long,
    val updatedAt: String,
)

data class ContentSearchResponse(
    val items: List<ContentArticleSummary>,
)

data class ContentArticleSummary(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String?,
    val lifeStage: String?,
    val contentType: String,
    val gestationalWeek: Int?,
)

data class ContentSourceResponse(
    val id: String,
    val citationKey: String,
    val title: String,
    val url: String?,
    val publicationDate: String?,
    val lastCheckedAt: String?,
    val notes: String?,
)

data class ContentBookmarkListResponse(
    val bookmarks: List<ContentArticleSummary>,
)

data class AdminLoginRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val password: String,
)

data class AdminAuthTokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long,
    val roles: List<String>,
)

data class CreateContentDocumentRequest(
    @field:NotBlank @field:Size(max = 200) val slug: String,
    @field:NotBlank val locale: String = "fa-IR",
    @field:NotBlank val title: String,
    val summary: String? = null,
    val bodyRichtext: String? = null,
    @field:NotBlank val contentType: String = "article",
    val lifeStage: String? = null,
    val gestationalWeek: Int? = null,
    val tags: List<String> = emptyList(),
    val medicalRiskLevel: MedicalRiskLevel = MedicalRiskLevel.GENERAL_EDUCATION,
    val sourceIds: List<String> = emptyList(),
)

data class AdminContentVersionResponse(
    val documentId: String,
    val versionId: String,
    val slug: String,
    val locale: String,
    val versionNumber: Int,
    val title: String,
    val summary: String?,
    val bodyRichtext: String?,
    val contentType: String,
    val lifeStage: String?,
    val gestationalWeek: Int?,
    val tags: List<String>,
    val medicalRiskLevel: MedicalRiskLevel,
    val status: ContentStatus,
    val reviewStage: String?,
    val sourceIds: List<String>,
    val clinicalReviewer: String?,
    val clinicalReviewedAt: String?,
    val nextReviewDueAt: String?,
    val published: Boolean,
)

data class WorkflowTransitionRequest(
    val clinicalReviewer: String? = null,
    val nextReviewDueAt: String? = null,
)

data class CreateContentSourceRequest(
    @field:NotBlank val citationKey: String,
    @field:NotBlank val title: String,
    val url: String? = null,
    val publicationDate: String? = null,
    val notes: String? = null,
)

data class ContentFreshnessDashboardResponse(
    val overdueClinicalReview: List<FreshnessItem>,
    val staleSources: List<FreshnessItem>,
    val recentlyWithdrawn: List<FreshnessItem>,
)

data class FreshnessItem(
    val documentId: String?,
    val versionId: String?,
    val sourceId: String?,
    val title: String,
    val detail: String,
)

data class ContentAuditListResponse(
    val events: List<ContentAuditEventResponse>,
)

data class ContentAuditEventResponse(
    val id: String,
    val action: String,
    val actorEmail: String?,
    val documentId: String?,
    val versionId: String?,
    val createdAt: String,
)

data class PregnancyWeekListResponse(
    val locale: String,
    val weeks: List<Int>,
)

enum class ReviewStage {
    @JsonProperty("medical")
    MEDICAL,

    @JsonProperty("editorial")
    EDITORIAL,
}

data class CreateCmsStaffRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val password: String,
    @field:NotBlank val displayName: String,
    val roles: List<String>,
)

data class PregnancyWeekQuery(
    @field:Min(1) @field:Max(42) val week: Int,
)
