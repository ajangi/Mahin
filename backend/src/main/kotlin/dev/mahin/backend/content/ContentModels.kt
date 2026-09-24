package dev.mahin.backend.content

import com.fasterxml.jackson.annotation.JsonProperty

enum class MedicalRiskLevel {
    @JsonProperty("none")
    NONE,

    @JsonProperty("general_education")
    GENERAL_EDUCATION,

    @JsonProperty("contact_clinician")
    CONTACT_CLINICIAN,

    @JsonProperty("urgent")
    URGENT,
}

enum class ContentStatus {
    @JsonProperty("draft")
    DRAFT,

    @JsonProperty("review")
    REVIEW,

    @JsonProperty("approved")
    APPROVED,

    @JsonProperty("published")
    PUBLISHED,

    @JsonProperty("retired")
    RETIRED,
}

data class ContentArticleResponse(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String?,
    val body: String?,
    val contentType: String,
    val lifeStage: String?,
    val medicalRiskLevel: MedicalRiskLevel,
    val status: ContentStatus,
    val contentVersion: Int,
    val sourceReferences: List<String>,
    val clinicalReviewer: String?,
    val clinicalReviewedAt: String?,
)
