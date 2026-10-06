package dev.mahin.core.designsystem

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.MahinTokenHex
import org.junit.Test

class MahinDarkSemanticContrastTest {
    private val darkSurfaces =
        listOf(
            MahinTokenHex.DARK_SURFACE_BACKGROUND,
            MahinTokenHex.DARK_SURFACE_DEFAULT,
            MahinTokenHex.DARK_SURFACE_ELEVATED,
        )

    private val darkSemantics =
        listOf(
            MahinTokenHex.DARK_HEALTH_PERIOD,
            MahinTokenHex.DARK_HEALTH_FERTILITY,
            MahinTokenHex.DARK_HEALTH_OVULATION,
            MahinTokenHex.DARK_HEALTH_PREGNANCY,
            MahinTokenHex.DARK_STATUS_POSITIVE,
            MahinTokenHex.DARK_STATUS_WARNING,
            MahinTokenHex.DARK_STATUS_CRITICAL,
        )

    @Test
    fun darkSemanticColours_meetTextContrastOnAllDarkSurfaces() {
        darkSemantics.forEach { semantic ->
            darkSurfaces.forEach { surface ->
                val ratio = contrastRatio(semantic, surface)
                assertThat(ratio).isAtLeast(4.5)
            }
        }
    }

    @Test
    fun darkFertilityAndOvulation_areVisuallyDistinct() {
        val delta =
            deltaE76(
                MahinTokenHex.DARK_HEALTH_FERTILITY,
                MahinTokenHex.DARK_HEALTH_OVULATION,
            )
        assertThat(delta).isAtLeast(10.0)
    }

    @Test
    fun darkPeriodAndCritical_areVisuallyDistinct() {
        val delta =
            deltaE76(
                MahinTokenHex.DARK_HEALTH_PERIOD,
                MahinTokenHex.DARK_STATUS_CRITICAL,
            )
        assertThat(delta).isAtLeast(10.0)
    }
}
