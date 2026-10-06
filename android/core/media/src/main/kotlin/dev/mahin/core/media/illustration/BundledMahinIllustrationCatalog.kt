package dev.mahin.core.media.illustration

import dev.mahin.core.media.R
import dev.mahin.core.model.MediaApprovalStatus

/**
 * M14b bundled vectors only. CMS-backed assets are resolved by future remote sources (M17).
 */
object BundledMahinIllustrationCatalog : MahinIllustrationSource {
    private val byId: Map<String, MahinIllustrationMetadata> =
        listOf(
            MahinIllustrationMetadata(
                assetId = "editorial/empty-state/v1",
                drawableRes = R.drawable.mahin_ill_placeholder_empty_state_v1,
                placeholder = true,
                approvalStatus = null,
                contentDescriptionRes = R.string.ill_desc_empty_state_placeholder,
            ),
            MahinIllustrationMetadata(
                assetId = "editorial/welcome/v1",
                drawableRes = R.drawable.mahin_ill_placeholder_editorial_v1,
                placeholder = true,
                approvalStatus = null,
                contentDescriptionRes = R.string.ill_desc_editorial_placeholder,
            ),
            MahinIllustrationMetadata(
                assetId = "pregnancy/fetal-development/week-18/v3",
                drawableRes = R.drawable.mahin_ill_placeholder_editorial_v1,
                placeholder = false,
                approvalStatus = MediaApprovalStatus.IN_REVIEW,
                contentDescriptionRes = R.string.ill_desc_medical_gated,
            ),
            MahinIllustrationMetadata(
                assetId = "cycle/education/ovulation/v2",
                drawableRes = R.drawable.mahin_ill_placeholder_editorial_v1,
                placeholder = false,
                approvalStatus = MediaApprovalStatus.DRAFT,
                contentDescriptionRes = R.string.ill_desc_medical_gated,
            ),
            MahinIllustrationMetadata(
                assetId = "pregnancy/placeholder/mislabelled/v1",
                drawableRes = R.drawable.mahin_ill_placeholder_editorial_v1,
                placeholder = true,
                approvalStatus = MediaApprovalStatus.APPROVED,
                contentDescriptionRes = R.string.ill_desc_medical_gated,
            ),
        ).associateBy { it.assetId }

    override fun metadataFor(assetId: String): MahinIllustrationMetadata? = byId[assetId]
}
