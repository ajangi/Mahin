package dev.mahin.backend.media

enum class MediaFamily {
    pregnancy_development,
    pregnancy_size,
    cycle_education,
    symptom_icon,
    editorial,
    motion,
    ui_core,
    non_medical_placeholder,
}

enum class MediaApprovalStatus {
    draft,
    in_review,
    approved,
    published,
    retired,
    rejected,
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
