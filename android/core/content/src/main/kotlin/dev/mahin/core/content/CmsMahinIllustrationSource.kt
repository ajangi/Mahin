package dev.mahin.core.content

import dev.mahin.core.config.MediaDeliveryConfig
import dev.mahin.core.media.illustration.BundledMahinIllustrationCatalog
import dev.mahin.core.media.illustration.MahinIllustrationMetadata
import dev.mahin.core.media.illustration.MahinIllustrationSource
import dev.mahin.core.media.illustration.isMedicalGovernedIllustrationId
import dev.mahin.core.media.MediaDeliveryUrlGuard
import dev.mahin.core.model.MediaApprovalStatus
import dev.mahin.core.model.MediaFamily
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CmsMahinIllustrationSource
    @Inject
    constructor(
        private val mediaMemoryCache: MediaAssetMemoryCache,
        private val mediaDeliveryConfig: MediaDeliveryConfig,
    ) : MahinIllustrationSource {
        override fun metadataFor(assetId: String): MahinIllustrationMetadata? {
            BundledMahinIllustrationCatalog.metadataFor(assetId)?.let { return it }
            val cached = mediaMemoryCache.get(assetId) ?: return null
            val approval =
                MediaApprovalStatus.entries.firstOrNull {
                    it.name.equals(cached.approvalStatus, ignoreCase = true)
                } ?: MediaApprovalStatus.DRAFT
            val familyKey = cached.family.uppercase().replace('-', '_')
            val family =
                MediaFamily.entries.firstOrNull { it.name == familyKey }
                    ?: MediaFamily.EDITORIAL
            val medical = cached.medicalGoverned || family == MediaFamily.PREGNANCY_DEVELOPMENT
            val url =
                cached.publicUrl?.takeIf { it.isNotBlank() }
                    ?: mediaDeliveryConfig.urlFor(cached.storageKey)
            if (!MediaDeliveryUrlGuard.isSafePublicMediaUrl(url)) {
                MediaDeliveryUrlGuard.logUnsafeUrl("cms_asset")
                return MahinIllustrationMetadata(
                    assetId = assetId,
                    drawableRes = null,
                    remoteUrl = null,
                    placeholder = family == MediaFamily.NON_MEDICAL_PLACEHOLDER,
                    approvalStatus = approval,
                    contentDescriptionRes = null,
                    altText = cached.altText,
                    medicalGoverned = medical || isMedicalGovernedIllustrationId(assetId),
                )
            }
            return MahinIllustrationMetadata(
                assetId = assetId,
                drawableRes = null,
                remoteUrl = url,
                placeholder = family == MediaFamily.NON_MEDICAL_PLACEHOLDER,
                approvalStatus = approval,
                contentDescriptionRes = null,
                altText = cached.altText,
                medicalGoverned = medical || isMedicalGovernedIllustrationId(assetId),
            )
        }
    }
