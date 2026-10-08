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

    @Test
    fun ltr_todayMarkerRotation_alignsWithArcFractions() {
        val geometry = mahinRingGeometry(LayoutDirection.Ltr)
        assertThat(mahinRingTodayMarkerRotationDegrees(geometry, 0f)).isEqualTo(0f)
        assertThat(mahinRingTodayMarkerRotationDegrees(geometry, 0.25f)).isEqualTo(67.5f)
        assertThat(mahinRingTodayMarkerRotationDegrees(geometry, 0.5f)).isEqualTo(135f)
    }

    @Test
    fun rtl_todayMarkerRotation_alignsWithArcFractions() {
        val geometry = mahinRingGeometry(LayoutDirection.Rtl)
        assertThat(mahinRingTodayMarkerRotationDegrees(geometry, 0f)).isEqualTo(0f)
        assertThat(mahinRingTodayMarkerRotationDegrees(geometry, 0.25f)).isEqualTo(-67.5f)
        assertThat(mahinRingTodayMarkerRotationDegrees(geometry, 0.5f)).isEqualTo(-135f)
    }
}
