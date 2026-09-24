package dev.mahin.core.designsystem

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.MahinTokenHex
import org.junit.Test

class MahinColorsTest {
    @Test
    fun composeColorsUseCanonicalHex() {
        assertThat(MahinLightColors.brandPrimary.value).isEqualTo(colorValue(MahinTokenHex.LIGHT_BRAND_PRIMARY))
        assertThat(MahinLightColors.surfaceBackground.value)
            .isEqualTo(colorValue(MahinTokenHex.LIGHT_SURFACE_BACKGROUND))
        assertThat(MahinLightColors.healthPeriod.value).isEqualTo(colorValue(MahinTokenHex.LIGHT_HEALTH_PERIOD))
    }

    @Test
    fun spacingFollowsEightDpGrid() {
        assertThat(MahinSpacing.xs.value).isEqualTo(8f)
        assertThat(MahinSpacing.md.value).isEqualTo(16f)
        assertThat(MahinSpacing.lg.value).isEqualTo(24f)
    }

    private fun colorValue(hex: String): ULong {
        val digits = hex.removePrefix("#")
        return ("FF$digits").toULong(16) shl 32
    }
}
