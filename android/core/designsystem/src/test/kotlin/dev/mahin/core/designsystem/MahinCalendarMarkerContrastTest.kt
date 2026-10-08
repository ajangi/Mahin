package dev.mahin.core.designsystem

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.designsystem.component.MahinCalendarMarkerTintAlphas
import dev.mahin.core.designsystem.component.blendCalendarMarkerFillSrgb
import dev.mahin.core.designsystem.component.mahinCalendarDayCellColors
import dev.mahin.core.designsystem.component.mahinCalendarDayLabelLuminanceThreshold
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
        lightSurfaces.forEach { surfaceHex ->
            val parent = hexToColor(surfaceHex)
            lightFills.forEach { (health, alpha) ->
                val blendedHex = compositeHexOver(health, alpha, surfaceHex)
                val blended = hexToColor(blendedHex)
                val markerFill = hexToColor(health).copy(alpha = alpha)
                val colors = mahinCalendarDayCellColors(parent, markerFill, estimatedOvulation = false)
                assertThat(contrastRatioBetweenColors(colors.label, blended)).isAtLeast(4.5)
            }
        }
    }

    @Test
    fun darkTheme_dayNumberText_onBlendedFills_meetsContrast() {
        darkSurfaces.forEach { surfaceHex ->
            val parent = hexToColor(surfaceHex)
            darkFills.forEach { (health, alpha) ->
                val blendedHex = compositeHexOver(health, alpha, surfaceHex)
                val blended = hexToColor(blendedHex)
                val markerFill = hexToColor(health).copy(alpha = alpha)
                val colors = mahinCalendarDayCellColors(parent, markerFill, estimatedOvulation = false)
                assertThat(contrastRatioBetweenColors(colors.label, blended)).isAtLeast(4.5)
            }
        }
    }

    @Test
    fun ovulationMarker_onOvulationFill_meetsGraphicsContrast_lightAndDark() {
        lightSurfaces.forEach { surfaceHex ->
            val parent = hexToColor(surfaceHex)
            val health = MahinTokenHex.LIGHT_HEALTH_OVULATION
            val alpha = MahinCalendarMarkerTintAlphas.LIGHT_OVULATION
            val blendedHex = compositeHexOver(health, alpha, surfaceHex)
            val blended = hexToColor(blendedHex)
            val markerFill = hexToColor(health).copy(alpha = alpha)
            val colors = mahinCalendarDayCellColors(parent, markerFill, estimatedOvulation = true)
            assertThat(colors.ovulationMarker).isNotNull()
            assertThat(contrastRatioBetweenColors(colors.ovulationMarker!!, blended)).isAtLeast(3.0)
        }
        darkSurfaces.forEach { surfaceHex ->
            val parent = hexToColor(surfaceHex)
            val health = MahinTokenHex.DARK_HEALTH_OVULATION
            val alpha = MahinCalendarMarkerTintAlphas.DARK_OVULATION
            val blendedHex = compositeHexOver(health, alpha, surfaceHex)
            val blended = hexToColor(blendedHex)
            val markerFill = hexToColor(health).copy(alpha = alpha)
            val colors = mahinCalendarDayCellColors(parent, markerFill, estimatedOvulation = true)
            assertThat(colors.ovulationMarker).isNotNull()
            assertThat(contrastRatioBetweenColors(colors.ovulationMarker!!, blended)).isAtLeast(3.0)
        }
    }

    @Test
    fun dayLabel_usesContrastHelper_notLuminanceThreshold_onOvulationFills() {
        lightSurfaces.forEach { surfaceHex ->
            val parent = hexToColor(surfaceHex)
            val health = MahinTokenHex.LIGHT_HEALTH_OVULATION
            val alpha = MahinCalendarMarkerTintAlphas.LIGHT_OVULATION
            val blended =
                blendCalendarMarkerFillSrgb(parent, hexToColor(health).copy(alpha = alpha))
            val production =
                mahinCalendarDayCellColors(
                    parent,
                    hexToColor(health).copy(alpha = alpha),
                    estimatedOvulation = false,
                ).label
            val threshold = mahinCalendarDayLabelLuminanceThreshold(blended)
            assertThat(colorToHex(compositeSrgbOver(production, blended))).isNotEqualTo(
                colorToHex(compositeSrgbOver(threshold, blended)),
            )
        }
    }
}
