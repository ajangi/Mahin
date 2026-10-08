package dev.mahin.core.designsystem

import com.google.common.truth.Truth.assertThat
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
    fun lightText_meetsContrastOnSurfaces() {
        lightSurfaces.forEach { surface ->
            assertThat(contrastRatio(MahinTokenHex.LIGHT_TEXT_PRIMARY, surface)).isAtLeast(4.5)
        }
    }

    @Test
    fun darkText_meetsContrastOnSurfaces() {
        darkSurfaces.forEach { surface ->
            assertThat(contrastRatio(MahinTokenHex.DARK_TEXT_PRIMARY, surface)).isAtLeast(4.5)
        }
    }

    @Test
    fun lightMarkerSemantics_meetGraphicsContrastOnSurfaces() {
        val markers =
            listOf(
                MahinTokenHex.LIGHT_HEALTH_PERIOD,
                MahinTokenHex.LIGHT_HEALTH_FERTILITY,
                MahinTokenHex.LIGHT_HEALTH_OVULATION,
            )
        markers.forEach { marker ->
            lightSurfaces.forEach { surface ->
                assertThat(contrastRatio(marker, surface)).isAtLeast(3.0)
            }
        }
    }

    @Test
    fun darkMarkerSemantics_meetGraphicsContrastOnSurfaces() {
        val markers =
            listOf(
                MahinTokenHex.DARK_HEALTH_PERIOD,
                MahinTokenHex.DARK_HEALTH_FERTILITY,
                MahinTokenHex.DARK_HEALTH_OVULATION,
            )
        markers.forEach { marker ->
            darkSurfaces.forEach { surface ->
                assertThat(contrastRatio(marker, surface)).isAtLeast(3.0)
            }
        }
    }
}
