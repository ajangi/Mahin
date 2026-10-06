package dev.mahin.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MahinTokenHexTest {
    @Test
    fun frozenLightBrandMatchesDesignSystem() {
        assertThat(MahinTokenHex.LIGHT_BRAND_PRIMARY).isEqualTo("#6E355D")
        assertThat(MahinTokenHex.LIGHT_SURFACE_BACKGROUND).isEqualTo("#FCF9F7")
        assertThat(MahinTokenHex.LIGHT_HEALTH_PERIOD).isEqualTo("#C94F62")
        assertThat(MahinTokenHex.LIGHT_HEALTH_FERTILITY).isEqualTo("#3B8F91")
        assertThat(MahinTokenHex.LIGHT_HEALTH_OVULATION).isEqualTo("#277276")
        assertThat(MahinTokenHex.LIGHT_HEALTH_PREGNANCY).isEqualTo("#E99A73")
    }

    @Test
    fun frozenDarkBaselineMatchesDesignSystem() {
        assertThat(MahinTokenHex.DARK_SURFACE_BACKGROUND).isEqualTo("#171417")
        assertThat(MahinTokenHex.DARK_BRAND_PRIMARY).isEqualTo("#D2A5C3")
        assertThat(MahinTokenHex.DARK_HEALTH_PERIOD).isEqualTo("#CC7582")
        assertThat(MahinTokenHex.DARK_STATUS_CRITICAL).isEqualTo("#D66F78")
    }
}

class MediaAssetRefTest {
    @Test
    fun unapprovedMedicalAssetIsNotAuthoritative() {
        val asset =
            MediaAssetRef(
                id = "00000000-0000-0000-0000-000000000001",
                storageKey = "pregnancy/fetal-development/week-18/v1",
                family = MediaFamily.PREGNANCY_DEVELOPMENT,
                version = 1,
                locale = "fa-IR",
                medicalGoverned = true,
                approvalStatus = MediaApprovalStatus.DRAFT,
                altText = "placeholder",
            )
        assertThat(asset.isAuthoritativeProductionContent).isFalse()
    }

    @Test
    fun nonMedicalPlaceholderCanRender() {
        val asset =
            MediaAssetRef(
                id = "00000000-0000-0000-0000-000000000002",
                storageKey = "placeholders/non-medical/foundation-mark/v1",
                family = MediaFamily.NON_MEDICAL_PLACEHOLDER,
                version = 1,
                locale = "fa-IR",
                medicalGoverned = false,
                approvalStatus = MediaApprovalStatus.DRAFT,
                altText = "decorative",
            )
        assertThat(asset.isAuthoritativeProductionContent).isTrue()
    }
}
