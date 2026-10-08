package dev.mahin.core.designsystem.component

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** Stroke width fraction used by [MahinCycleProgressRing]. */
fun mahinRingStrokeWidth(side: Float): Float = side * 0.09f

/** Canvas centre for ring geometry (matches [androidx.compose.ui.graphics.drawscope.DrawScope] size). */
fun mahinRingTodayMarkerCenter(side: Float): Offset = Offset(side / 2f, side / 2f)

/** Arc radius for [androidx.compose.ui.graphics.drawscope.drawArc] with [mahinRingStrokeWidth]. */
fun mahinRingArcRadius(
    side: Float,
    stroke: Float = mahinRingStrokeWidth(side),
): Float = (side - stroke) / 2f

/**
 * Point on the ring arc at [fraction] along [geometry.sweepTotal], matching [drawArc] endpoint
 * (same centre, radius, and angle convention as [MahinCycleProgressRing]).
 */
fun mahinRingTodayMarkerPoint(
    side: Float,
    geometry: MahinRingGeometry,
    fraction: Float,
): Offset {
    val stroke = mahinRingStrokeWidth(side)
    val center = mahinRingTodayMarkerCenter(side)
    val radius = mahinRingArcRadius(side, stroke)
    val angleDeg = geometry.startAngle + fraction.coerceIn(0f, 1f) * geometry.sweepTotal
    val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
    return Offset(
        center.x + radius * cos(angleRad),
        center.y + radius * sin(angleRad),
    )
}

/** Distance from [mahinRingTodayMarkerCenter] to [point] (should equal arc radius). */
fun mahinRingMarkerDistanceFromCenter(
    side: Float,
    point: Offset,
): Float {
    val center = mahinRingTodayMarkerCenter(side)
    return hypot(point.x - center.x, point.y - center.y)
}
