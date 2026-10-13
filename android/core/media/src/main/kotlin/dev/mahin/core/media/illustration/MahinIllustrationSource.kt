package dev.mahin.core.media.illustration

import dev.mahin.core.model.MediaApprovalStatus

/**
 * Bundled illustration metadata for M14b. Remote CMS sources implement [MahinIllustrationSource] later (M17).
 */
data class MahinIllustrationMetadata(
    val assetId: String,
    val drawableRes: Int?,
    val remoteUrl: String? = null,
    val placeholder: Boolean,
    val approvalStatus: MediaApprovalStatus?,
    val contentDescriptionRes: Int?,
    val altText: String? = null,
    val medicalGoverned: Boolean = false,
)

/**
 * Resolves stable semantic asset IDs to bundled metadata. Network/CDN implementations come in M17.
 */
interface MahinIllustrationSource {
    fun metadataFor(assetId: String): MahinIllustrationMetadata?
}

fun isMedicalGovernedIllustrationId(assetId: String): Boolean =
    assetId.startsWith("pregnancy/") || assetId.startsWith("cycle/education/")

/**
 * Whether [metadata] may render bundled imagery inside the illustration slot.
 * [MediaApprovalStatus.PUBLISHED] is treated like [MediaApprovalStatus.APPROVED] for medical assets.
 * Unknown IDs and gated medical assets fail safe to the neutral frame without imagery.
 */
fun MahinIllustrationMetadata.mayRenderBundledImagery(): Boolean {
    if (drawableRes == null) return false
    return mayRenderImagery()
}

fun MahinIllustrationMetadata.mayRenderImagery(): Boolean {
    if (drawableRes == null && remoteUrl.isNullOrBlank()) return false
    val medical = medicalGoverned || isMedicalGovernedIllustrationId(assetId)
    return when {
        placeholder -> !medical
        !medical -> true
        approvalStatus == MediaApprovalStatus.APPROVED ||
            approvalStatus == MediaApprovalStatus.PUBLISHED -> true
        else -> false
    }
}
