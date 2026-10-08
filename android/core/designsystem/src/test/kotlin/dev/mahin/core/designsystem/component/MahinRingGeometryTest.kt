package dev.mahin.core.designsystem.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.LayoutDirection
import com.google.common.truth.Truth.assertThat
import kotlin.math.abs
import org.junit.Test

class MahinRingGeometryTest {
    private val side = 220f
    private val stroke = mahinRingStrokeWidth(side)

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
    fun ltr_markerPoint_atOneThird_isThreeOClock() {
        val geometry = mahinRingGeometry(LayoutDirection.Ltr)
        val point = mahinRingTodayMarkerPoint(side, geometry, 1f / 3f)
        assertThat(point.x).isWithin(0.001f).of(side - stroke / 2f)
        assertThat(point.y).isWithin(0.001f).of(side / 2f)
    }

    @Test
    fun rtl_markerPoint_atOneThird_isNineOClock() {
        val geometry = mahinRingGeometry(LayoutDirection.Rtl)
        val point = mahinRingTodayMarkerPoint(side, geometry, 1f / 3f)
        assertThat(point.x).isWithin(0.001f).of(stroke / 2f)
        assertThat(point.y).isWithin(0.001f).of(side / 2f)
    }

    @Test
    fun ltr_markerPoints_atHalfAndFull_matchExactCoordinates() {
        val geometry = mahinRingGeometry(LayoutDirection.Ltr)
        val half = mahinRingTodayMarkerPoint(side, geometry, 0.5f)
        val end = mahinRingTodayMarkerPoint(side, geometry, 1f)
        val radius = mahinRingArcRadius(side, stroke)
        val center = mahinRingTodayMarkerCenter(side)
        val expectedHalf =
            Offset(
                center.x + radius * kotlin.math.cos(Math.toRadians(45.0)).toFloat(),
                center.y + radius * kotlin.math.sin(Math.toRadians(45.0)).toFloat(),
            )
        assertThat(half.x).isWithin(0.001f).of(expectedHalf.x)
        assertThat(half.y).isWithin(0.001f).of(expectedHalf.y)
        assertThat(end.x).isWithin(0.001f).of(stroke / 2f)
        assertThat(end.y).isWithin(0.001f).of(side / 2f)
    }

    @Test
    fun rtl_markerPoints_atHalfAndFull_matchExactCoordinates() {
        val geometry = mahinRingGeometry(LayoutDirection.Rtl)
        val half = mahinRingTodayMarkerPoint(side, geometry, 0.5f)
        val end = mahinRingTodayMarkerPoint(side, geometry, 1f)
        val radius = mahinRingArcRadius(side, stroke)
        val center = mahinRingTodayMarkerCenter(side)
        val expectedHalf =
            Offset(
                center.x + radius * kotlin.math.cos(Math.toRadians(-225.0)).toFloat(),
                center.y + radius * kotlin.math.sin(Math.toRadians(-225.0)).toFloat(),
            )
        assertThat(half.x).isWithin(0.001f).of(expectedHalf.x)
        assertThat(half.y).isWithin(0.001f).of(expectedHalf.y)
        assertThat(end.x).isWithin(0.001f).of(side - stroke / 2f)
        assertThat(end.y).isWithin(0.001f).of(side / 2f)
    }

    @Test
    fun markerPoints_stayOnArcRadius() {
        val geometry = mahinRingGeometry(LayoutDirection.Ltr)
        listOf(0f, 1f / 3f, 0.5f, 1f).forEach { fraction ->
            val point = mahinRingTodayMarkerPoint(side, geometry, fraction)
            val distance = mahinRingMarkerDistanceFromCenter(side, point)
            assertThat(abs(distance - mahinRingArcRadius(side, stroke))).isLessThan(0.05f)
        }
    }
}
