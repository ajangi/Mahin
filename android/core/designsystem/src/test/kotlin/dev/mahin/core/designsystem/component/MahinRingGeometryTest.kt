package dev.mahin.core.designsystem.component

import androidx.compose.ui.unit.LayoutDirection
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MahinRingGeometryTest {
    @Test
    fun ltr_sweepsClockwise_positiveSweep() {
        val geometry = mahinRingGeometry(LayoutDirection.Ltr)
        assertThat(geometry.startAngle).isEqualTo(-90f)
        assertThat(geometry.sweepTotal).isEqualTo(270f)
    }

    @Test
    fun rtl_sweepsCounterClockwise_negativeSweep() {
        val geometry = mahinRingGeometry(LayoutDirection.Rtl)
        assertThat(geometry.startAngle).isEqualTo(-90f)
        assertThat(geometry.sweepTotal).isEqualTo(-270f)
    }
}
