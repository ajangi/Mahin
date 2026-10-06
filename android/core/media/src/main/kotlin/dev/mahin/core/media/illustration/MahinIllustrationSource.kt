package dev.mahin.core.media.illustration

import dev.mahin.core.model.MediaApprovalStatus

/**
 * Bundled illustration metadata for M14b. Remote CMS sources implement [MahinIllustrationSource] later (M17).
 */
data class MahinIllustrationMetadata(
    val assetId: String,
    val drawableRes: Int?,
    val placeholder: Boolean,
    val approvalStatus: MediaApprovalStatus?,
    val contentDescriptionRes: Int?,
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
 * Unknown IDs and gated medical assets fail safe to the neutral frame without imagery.
 */
fun MahinIllustrationMetadata.mayRenderBundledImagery(): Boolean {
    if (drawableRes == null) return false
    val medical = isMedicalGovernedIllustrationId(assetId)
    return when {
        placeholder -> !medical
        !medical -> true
        approvalStatus == MediaApprovalStatus.APPROVED ||
            approvalStatus == MediaApprovalStatus.PUBLISHED -> true
        else -> false
    }
}
