package dev.mahin.core.content

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.config.AppEnvironment
import dev.mahin.core.config.MediaDeliveryConfig
import dev.mahin.core.media.illustration.mayRenderImagery
import org.junit.Test

class CmsMahinIllustrationSourceTest {
    private val cache = MediaAssetMemoryCache()
    private val source =
        CmsMahinIllustrationSource(
            mediaMemoryCache = cache,
            mediaDeliveryConfig =
                MediaDeliveryConfig(
                    publicBaseUrl = "http://localhost:9000/mahin-media",
                    environment = AppEnvironment.LOCAL,
                ),
        )

    @Test
    fun remoteMetadata_omitsUserIdentifiersFromUrl() {
        cache.put(
            MediaAssetDto(
                id = "asset-1",
                storageKey = "placeholders/non-medical/foundation-mark/v1",
                family = "editorial",
                version = 1,
                locale = "fa-IR",
                approvalStatus = "published",
                medicalGoverned = false,
                altText = "نمونه",
            ),
        )
        val meta = source.metadataFor("asset-1")
        assertThat(meta).isNotNull()
        assertThat(meta!!.remoteUrl).doesNotContain("user")
        assertThat(meta.remoteUrl).doesNotContain("?")
    }

    @Test
    fun unsafeUrl_failsSafeWithoutRemoteImagery() {
        cache.put(
            MediaAssetDto(
                id = "asset-unsafe",
                storageKey = "cycle/editorial/hero/v1",
                family = "editorial",
                version = 1,
                locale = "fa-IR",
                approvalStatus = "published",
                medicalGoverned = false,
                altText = "نمونه",
                publicUrl = "http://localhost:9000/mahin-media/cycle/hero?sig=1",
            ),
        )
        val meta = source.metadataFor("asset-unsafe")
        assertThat(meta).isNotNull()
        assertThat(meta!!.remoteUrl).isNull()
        assertThat(meta.mayRenderImagery()).isFalse()
    }

    @Test
    fun unapprovedMedicalAsset_failsSafeWithoutImagery() {
        cache.put(
            MediaAssetDto(
                id = "asset-2",
                storageKey = "pregnancy/fetal-development/week-18/v1",
                family = "pregnancy_development",
                version = 1,
                locale = "fa-IR",
                approvalStatus = "draft",
                medicalGoverned = true,
                altText = "gated",
            ),
        )
        val meta = source.metadataFor("asset-2")
        assertThat(meta).isNotNull()
        assertThat(meta!!.mayRenderImagery()).isFalse()
    }
}
