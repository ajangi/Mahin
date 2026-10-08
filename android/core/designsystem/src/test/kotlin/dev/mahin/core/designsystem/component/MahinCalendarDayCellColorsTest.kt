package dev.mahin.core.designsystem.component

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.designsystem.colorToHex
import dev.mahin.core.designsystem.compositeSrgbOver
import dev.mahin.core.designsystem.hexToColor
import dev.mahin.core.model.MahinTokenHex
import org.junit.Test

class MahinCalendarDayCellColorsTest {
    @Test
    fun ovulationLight_usesContrastPath_notLuminanceThreshold() {
        val parent = hexToColor(MahinTokenHex.LIGHT_SURFACE_DEFAULT)
        val fill =
            hexToColor(MahinTokenHex.LIGHT_HEALTH_OVULATION).copy(
                alpha = MahinCalendarMarkerTintAlphas.LIGHT_OVULATION,
            )
        val blended = blendCalendarMarkerFillSrgb(parent, fill)
        val production = mahinCalendarDayCellColors(parent, fill, estimatedOvulation = false).label
        val threshold = mahinCalendarDayLabelLuminanceThreshold(blended)
        assertThat(colorToHex(compositeSrgbOver(production, blended))).isNotEqualTo(
            colorToHex(compositeSrgbOver(threshold, blended)),
        )
    }
}
