package dev.mahin.backend.content

enum class MedicalRiskLevel {
    none,
    general_education,
    contact_clinician,
    urgent,
}

enum class ContentStatus {
    draft,
    review,
    approved,
    published,
    retired,
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
