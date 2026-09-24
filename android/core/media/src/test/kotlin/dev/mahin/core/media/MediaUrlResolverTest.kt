package dev.mahin.core.media

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.config.AppEnvironment
import dev.mahin.core.config.MediaDeliveryConfig
import dev.mahin.core.model.MediaApprovalStatus
import dev.mahin.core.model.MediaAssetRef
import dev.mahin.core.model.MediaFamily
import org.junit.Test

class MediaUrlResolverTest {
    private val resolver = ConfiguredMediaUrlResolver(
        MediaDeliveryConfig(
            publicBaseUrl = "http://localhost:9000/mahin-media",
            environment = AppEnvironment.LOCAL,
        ),
    )

    @Test
    fun doesNotResolveUnapprovedMedicalAssets() {
        val asset = MediaAssetRef(
            id = "1",
            storageKey = "pregnancy/fetal-development/week-18/v1",
            family = MediaFamily.PREGNANCY_DEVELOPMENT,
            version = 1,
            locale = "fa-IR",
            medicalGoverned = true,
            approvalStatus = MediaApprovalStatus.DRAFT,
            altText = "not approved",
        )
        assertThat(resolver.resolve(asset)).isNull()
    }

    @Test
    fun resolvesPlaceholderWithoutQueryOrUserId() {
        val asset = MediaAssetRef(
            id = "2",
            storageKey = "placeholders/non-medical/foundation-mark/v1",
            family = MediaFamily.NON_MEDICAL_PLACEHOLDER,
            version = 1,
            locale = "fa-IR",
            medicalGoverned = false,
            approvalStatus = MediaApprovalStatus.DRAFT,
            altText = "decorative mark",
        )
        val request = resolver.resolve(asset)
        assertThat(request).isNotNull()
        assertThat(request!!.url).doesNotContain("?")
        assertThat(request.url).doesNotContain("user")
        assertThat(request.url).endsWith("placeholders/non-medical/foundation-mark/v1")
    }
}
