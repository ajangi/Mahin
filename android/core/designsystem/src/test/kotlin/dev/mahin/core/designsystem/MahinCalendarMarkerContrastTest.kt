package dev.mahin.core.designsystem

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.designsystem.component.MahinCalendarMarkerTintAlphas
import dev.mahin.core.designsystem.component.mahinCalendarDayLabelLuminanceThreshold
import dev.mahin.core.designsystem.component.mahinCalendarDayLabelOnBlendedFill
import dev.mahin.core.designsystem.component.mahinCalendarOvulationMarkerOnBlendedFill
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

    private val lightFills =
        listOf(
            MahinTokenHex.LIGHT_HEALTH_PERIOD to MahinCalendarMarkerTintAlphas.LIGHT_PERIOD,
            MahinTokenHex.LIGHT_HEALTH_PERIOD to MahinCalendarMarkerTintAlphas.LIGHT_PERIOD_PREDICTED,
            MahinTokenHex.LIGHT_HEALTH_FERTILITY to MahinCalendarMarkerTintAlphas.LIGHT_FERTILE,
            MahinTokenHex.LIGHT_HEALTH_OVULATION to MahinCalendarMarkerTintAlphas.LIGHT_OVULATION,
        )

    private val darkFills =
        listOf(
            MahinTokenHex.DARK_HEALTH_PERIOD to MahinCalendarMarkerTintAlphas.DARK_PERIOD,
            MahinTokenHex.DARK_HEALTH_PERIOD to MahinCalendarMarkerTintAlphas.DARK_PERIOD_PREDICTED,
            MahinTokenHex.DARK_HEALTH_FERTILITY to MahinCalendarMarkerTintAlphas.DARK_FERTILE,
            MahinTokenHex.DARK_HEALTH_OVULATION to MahinCalendarMarkerTintAlphas.DARK_OVULATION,
        )

    @Test
    fun lightTheme_dayNumberText_onBlendedFills_meetsContrast() {
        lightSurfaces.forEach { surface ->
            lightFills.forEach { (health, alpha) ->
                val blended = compositeHexOver(health, alpha, surface)
                val blendedColor = hexToColor(blended)
                val text = mahinCalendarDayLabelOnBlendedFill(blendedColor)
                assertThat(contrastRatioBetweenColors(text, blendedColor)).isAtLeast(4.5)
            }
        }
    }

    @Test
    fun darkTheme_dayNumberText_onBlendedFills_meetsContrast() {
        darkSurfaces.forEach { surface ->
            darkFills.forEach { (health, alpha) ->
                val blended = compositeHexOver(health, alpha, surface)
                val blendedColor = hexToColor(blended)
                val text = mahinCalendarDayLabelOnBlendedFill(blendedColor)
                assertThat(contrastRatioBetweenColors(text, blendedColor)).isAtLeast(4.5)
            }
        }
    }

    @Test
    fun ovulationMarker_onOvulationFill_meetsGraphicsContrast_lightAndDark() {
        lightSurfaces.forEach { surface ->
            val blended =
                compositeHexOver(
                    MahinTokenHex.LIGHT_HEALTH_OVULATION,
                    MahinCalendarMarkerTintAlphas.LIGHT_OVULATION,
                    surface,
                )
            val blendedColor = hexToColor(blended)
            val marker = mahinCalendarOvulationMarkerOnBlendedFill(blendedColor)
            assertThat(contrastRatioBetweenColors(marker, blendedColor)).isAtLeast(3.0)
        }
        darkSurfaces.forEach { surface ->
            val blended =
                compositeHexOver(
                    MahinTokenHex.DARK_HEALTH_OVULATION,
                    MahinCalendarMarkerTintAlphas.DARK_OVULATION,
                    surface,
                )
            val blendedColor = hexToColor(blended)
            val marker = mahinCalendarOvulationMarkerOnBlendedFill(blendedColor)
            assertThat(contrastRatioBetweenColors(marker, blendedColor)).isAtLeast(3.0)
        }
    }

    @Test
    fun dayLabel_usesContrastHelper_notLuminanceThreshold_onOvulationFills() {
        lightSurfaces.forEach { surface ->
            val blended =
                compositeHexOver(
                    MahinTokenHex.LIGHT_HEALTH_OVULATION,
                    MahinCalendarMarkerTintAlphas.LIGHT_OVULATION,
                    surface,
                )
            val blendedColor = hexToColor(blended)
            val production = mahinCalendarDayLabelOnBlendedFill(blendedColor)
            val threshold = mahinCalendarDayLabelLuminanceThreshold(blendedColor)
            assertThat(colorToHex(production)).isNotEqualTo(colorToHex(threshold))
        }
    }
}
