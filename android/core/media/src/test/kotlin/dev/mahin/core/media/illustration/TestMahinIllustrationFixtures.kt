package dev.mahin.core.media.illustration

import dev.mahin.core.media.R
import dev.mahin.core.model.MediaApprovalStatus

/** Test-only illustration metadata (not shipped in production catalog). */
object TestMahinIllustrationFixtures {
    const val PREGNANCY_UNAPPROVED = "pregnancy/fetal-development/week-18/v3"
    const val CYCLE_EDUCATION_DRAFT = "cycle/education/ovulation/v2"
    const val PREGNANCY_MISLABELLED_PLACEHOLDER = "pregnancy/placeholder/mislabelled/v1"
    const val PREGNANCY_UNKNOWN = "pregnancy/unknown/v1"
    const val PREGNANCY_APPROVED = "pregnancy/education/approved-sample/v1"

    val gatedCatalog: MahinIllustrationSource =
        object : MahinIllustrationSource {
            override fun metadataFor(assetId: String): MahinIllustrationMetadata? =
                when (assetId) {
                    PREGNANCY_UNAPPROVED ->
                        MahinIllustrationMetadata(
                            assetId = assetId,
                            drawableRes = R.drawable.mahin_ill_placeholder_editorial_v1,
                            placeholder = false,
                            approvalStatus = MediaApprovalStatus.IN_REVIEW,
                            contentDescriptionRes = R.string.ill_desc_medical_gated,
                        )
                    CYCLE_EDUCATION_DRAFT ->
                        MahinIllustrationMetadata(
                            assetId = assetId,
                            drawableRes = R.drawable.mahin_ill_placeholder_editorial_v1,
                            placeholder = false,
                            approvalStatus = MediaApprovalStatus.DRAFT,
                            contentDescriptionRes = R.string.ill_desc_medical_gated,
                        )
                    PREGNANCY_MISLABELLED_PLACEHOLDER ->
                        MahinIllustrationMetadata(
                            assetId = assetId,
                            drawableRes = R.drawable.mahin_ill_placeholder_editorial_v1,
                            placeholder = true,
                            approvalStatus = MediaApprovalStatus.APPROVED,
                            contentDescriptionRes = R.string.ill_desc_medical_gated,
                        )
                    PREGNANCY_UNKNOWN -> null
                    PREGNANCY_APPROVED ->
                        MahinIllustrationMetadata(
                            assetId = assetId,
                            drawableRes = R.drawable.mahin_ill_placeholder_editorial_v1,
                            placeholder = false,
                            approvalStatus = MediaApprovalStatus.PUBLISHED,
                            contentDescriptionRes = R.string.ill_desc_medical_gated,
                        )
                    else -> null
                }
        }
}
