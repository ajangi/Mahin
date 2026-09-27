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
    ;

    fun wireValue(): String =
        when (this) {
            NONE -> "none"
            GENERAL_EDUCATION -> "general_education"
            CONTACT_CLINICIAN -> "contact_clinician"
            URGENT -> "urgent"
        }
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
