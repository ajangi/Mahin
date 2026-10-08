package dev.mahin.core.designsystem

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.designsystem.component.MahinCalendarMarkerTintAlphas
import dev.mahin.core.model.MahinTokenHex
import org.junit.Test

class MahinCalendarMarkerContrastTest {
    private val lightSurfaces =
        listOf(
            MahinTokenHex.LIGHT_SURFACE_BACKGROUND,
            MahinTokenHex.LIGHT_SURFACE_DEFAULT,
            MahinTokenHex.LIGHT_SURFACE_SECONDARY,
        )

    private val darkSurfaces =
        listOf(
            MahinTokenHex.DARK_SURFACE_BACKGROUND,
            MahinTokenHex.DARK_SURFACE_DEFAULT,
            MahinTokenHex.DARK_SURFACE_ELEVATED,
        )

    @Test
    fun lightTheme_dayNumberText_onBlendedFills_meetsContrast() {
        val fills =
            listOf(
                MahinTokenHex.LIGHT_HEALTH_PERIOD to MahinCalendarMarkerTintAlphas.LIGHT_PERIOD,
                MahinTokenHex.LIGHT_HEALTH_PERIOD to MahinCalendarMarkerTintAlphas.LIGHT_PERIOD_PREDICTED,
                MahinTokenHex.LIGHT_HEALTH_FERTILITY to MahinCalendarMarkerTintAlphas.LIGHT_FERTILE,
                MahinTokenHex.LIGHT_HEALTH_OVULATION to MahinCalendarMarkerTintAlphas.LIGHT_OVULATION,
            )
        lightSurfaces.forEach { surface ->
            fills.forEach { (health, alpha) ->
                val blended = compositeHexOver(health, alpha, surface)
                val text = preferredCalendarLabelHex(blended, isDark = false)
                assertThat(contrastRatio(text, blended)).isAtLeast(4.5)
            }
        }
    }

    @Test
    fun darkTheme_dayNumberText_onBlendedFills_meetsContrast() {
        val fills =
            listOf(
                MahinTokenHex.DARK_HEALTH_PERIOD to MahinCalendarMarkerTintAlphas.DARK_PERIOD,
                MahinTokenHex.DARK_HEALTH_PERIOD to MahinCalendarMarkerTintAlphas.DARK_PERIOD_PREDICTED,
                MahinTokenHex.DARK_HEALTH_FERTILITY to MahinCalendarMarkerTintAlphas.DARK_FERTILE,
                MahinTokenHex.DARK_HEALTH_OVULATION to MahinCalendarMarkerTintAlphas.DARK_OVULATION,
            )
        darkSurfaces.forEach { surface ->
            fills.forEach { (health, alpha) ->
                val blended = compositeHexOver(health, alpha, surface)
                val text = preferredCalendarLabelHex(blended, isDark = true)
                assertThat(contrastRatio(text, blended)).isAtLeast(4.5)
            }
        }
    }

    @Test
    fun ovulationMarker_onOvulationFill_meetsGraphicsContrast_lightAndDark() {
        lightSurfaces.forEach { surface ->
            val fill =
                compositeHexOver(
                    MahinTokenHex.LIGHT_HEALTH_OVULATION,
                    MahinCalendarMarkerTintAlphas.LIGHT_OVULATION,
                    surface,
                )
            val marker = preferredOvulationMarkerHex(fill, isDark = false)
            assertThat(contrastRatio(marker, fill)).isAtLeast(3.0)
        }
        darkSurfaces.forEach { surface ->
            val fill =
                compositeHexOver(
                    MahinTokenHex.DARK_HEALTH_OVULATION,
                    MahinCalendarMarkerTintAlphas.DARK_OVULATION,
                    surface,
                )
            val marker = preferredOvulationMarkerHex(fill, isDark = true)
            assertThat(contrastRatio(marker, fill)).isAtLeast(3.0)
        }
    }

    private fun preferredCalendarLabelHex(
        blendedBackgroundHex: String,
        isDark: Boolean,
    ): String {
        val candidates =
            listOf(
                if (isDark) MahinTokenHex.DARK_TEXT_PRIMARY else MahinTokenHex.LIGHT_TEXT_PRIMARY,
                "#FFFFFF",
                "#000000",
            )
        return candidates.maxBy { contrastRatio(it, blendedBackgroundHex) }
    }

    private fun preferredOvulationMarkerHex(
        blendedFillHex: String,
        isDark: Boolean,
    ): String {
        val candidates =
            listOf(
                if (isDark) MahinTokenHex.DARK_HEALTH_OVULATION else MahinTokenHex.LIGHT_HEALTH_OVULATION,
                "#FFFFFF",
                "#000000",
            )
        return candidates.first { contrastRatio(it, blendedFillHex) >= 3.0 }
    }
}
