package dev.mahin.backend.media

import com.fasterxml.jackson.annotation.JsonProperty

enum class MediaFamily {
    @JsonProperty("pregnancy_development")
    PREGNANCY_DEVELOPMENT,

    @JsonProperty("pregnancy_size")
    PREGNANCY_SIZE,

    @JsonProperty("cycle_education")
    CYCLE_EDUCATION,

    @JsonProperty("symptom_icon")
    SYMPTOM_ICON,

    @JsonProperty("editorial")
    EDITORIAL,

    @JsonProperty("motion")
    MOTION,

    @JsonProperty("ui_core")
    UI_CORE,

    @JsonProperty("non_medical_placeholder")
    NON_MEDICAL_PLACEHOLDER,
}

enum class MediaApprovalStatus {
    @JsonProperty("draft")
    DRAFT,

    @JsonProperty("in_review")
    IN_REVIEW,

    @JsonProperty("approved")
    APPROVED,

    @JsonProperty("published")
    PUBLISHED,

    @JsonProperty("retired")
    RETIRED,

    @JsonProperty("rejected")
    REJECTED,
}

data class MediaAssetResponse(
    val id: String,
    val storageKey: String,
    val family: MediaFamily,
    val version: Int,
    val locale: String,
    val publicUrl: String,
    val medicalGoverned: Boolean,
    val approvalStatus: MediaApprovalStatus,
    val sourceReferences: List<String>,
    val reviewer: String?,
    val reviewedAt: String?,
    val altText: String,
    val createdAt: String,
    val updatedAt: String,
    val supersededBy: String?,
)
