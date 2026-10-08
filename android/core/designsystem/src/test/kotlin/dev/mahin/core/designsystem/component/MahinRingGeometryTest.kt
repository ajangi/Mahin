package dev.mahin.core.designsystem.component

import androidx.compose.ui.unit.LayoutDirection
import com.google.common.truth.Truth.assertThat
import kotlin.math.abs
import org.junit.Test

class MahinRingGeometryTest {
    private val side = 220f
    private val stroke = mahinRingStrokeWidth(side)
    private val radius = mahinRingArcRadius(side, stroke)

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

    @Test
    fun todayMarkerCenter_isCanvasCenter() {
        val center = mahinRingTodayMarkerCenter(side)
        assertThat(center.x).isEqualTo(side / 2f)
        assertThat(center.y).isEqualTo(side / 2f)
    }

    @Test
    fun ltr_markerPoint_atFractionZero_isTopOfArc() {
        val geometry = mahinRingGeometry(LayoutDirection.Ltr)
        val point = mahinRingTodayMarkerPoint(side, geometry, 0f)
        assertThat(point.x).isWithin(0.01f).of(side / 2f)
        assertThat(point.y).isWithin(0.01f).of(stroke / 2f)
    }

    @Test
    fun ltr_markerPoints_areOnArc_atQuarterHalfAndFull() {
        val geometry = mahinRingGeometry(LayoutDirection.Ltr)
        listOf(0f, 0.25f, 0.5f, 1f).forEach { fraction ->
            val point = mahinRingTodayMarkerPoint(side, geometry, fraction)
            val distance = mahinRingMarkerDistanceFromCenter(side, point)
            assertThat(abs(distance - radius)).isLessThan(0.05f)
        }
    }

    @Test
    fun rtl_markerPoints_areOnArc_atQuarterHalfAndFull() {
        val geometry = mahinRingGeometry(LayoutDirection.Rtl)
        listOf(0f, 0.25f, 0.5f, 1f).forEach { fraction ->
            val point = mahinRingTodayMarkerPoint(side, geometry, fraction)
            val distance = mahinRingMarkerDistanceFromCenter(side, point)
            assertThat(abs(distance - radius)).isLessThan(0.05f)
        }
    }
}
