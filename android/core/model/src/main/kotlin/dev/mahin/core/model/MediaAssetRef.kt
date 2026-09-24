package dev.mahin.core.model

enum class MediaFamily {
    PREGNANCY_DEVELOPMENT,
    PREGNANCY_SIZE,
    CYCLE_EDUCATION,
    SYMPTOM_ICON,
    EDITORIAL,
    MOTION,
    UI_CORE,
    NON_MEDICAL_PLACEHOLDER,
}

enum class MediaApprovalStatus {
    DRAFT,
    IN_REVIEW,
    APPROVED,
    PUBLISHED,
    RETIRED,
    REJECTED,
}

/**
 * CMS media metadata. [storageKey] is the durable identifier; URLs are resolved at runtime.
 * Unapproved medical-governed assets must never be shown as authoritative content.
 */
data class MediaAssetRef(
    val id: String,
    val storageKey: String,
    val family: MediaFamily,
    val version: Int,
    val locale: String,
    val medicalGoverned: Boolean,
    val approvalStatus: MediaApprovalStatus,
    val altText: String,
    val widthPx: Int? = null,
    val heightPx: Int? = null,
) {
    val isAuthoritativeProductionContent: Boolean
        get() = !medicalGoverned ||
            approvalStatus == MediaApprovalStatus.APPROVED ||
            approvalStatus == MediaApprovalStatus.PUBLISHED
}
