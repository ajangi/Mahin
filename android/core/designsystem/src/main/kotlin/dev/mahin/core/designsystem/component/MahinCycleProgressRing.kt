package dev.mahin.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.mahin.core.designsystem.LocalReducedMotion
import dev.mahin.core.designsystem.MahinMotionDuration
import dev.mahin.core.designsystem.mahinMotionDurationMs

enum class MahinRingArcStyle {
    Solid,
    Dashed,
}

data class MahinRingArc(
    val color: Color,
    val startFraction: Float,
    val endFraction: Float,
    val style: MahinRingArcStyle = MahinRingArcStyle.Solid,
    val strokeWidthFraction: Float = 0.09f,
)

data class MahinRingGeometry(
    val startAngle: Float,
    val sweepTotal: Float,
)

/** 12 o'clock start; LTR sweeps clockwise, RTL counter-clockwise (mirrored). */
fun mahinRingGeometry(layoutDirection: LayoutDirection): MahinRingGeometry =
    when (layoutDirection) {
        LayoutDirection.Rtl ->
            MahinRingGeometry(
                startAngle = -90f,
                sweepTotal = -270f,
            )
        else ->
            MahinRingGeometry(
                startAngle = -90f,
                sweepTotal = 270f,
            )
    }

/** Degrees for [androidx.compose.ui.graphics.drawscope.rotate] so the today marker aligns with [drawArc] at [fraction]. */
fun mahinRingTodayMarkerRotationDegrees(
    geometry: MahinRingGeometry,
    fraction: Float,
): Float {
    val f = fraction.coerceIn(0f, 1f)
    return geometry.startAngle + f * geometry.sweepTotal + 90f
}

@Suppress("LongParameterList", "LongMethod")
@Composable
fun MahinCycleProgressRing(
    arcs: List<MahinRingArc>,
    progressFraction: Float,
    modifier: Modifier = Modifier,
    ringSize: Dp = 220.dp,
    trackColor: Color = Color.Transparent,
    progressColor: Color? = null,
    todayMarkerFraction: Float? = null,
    todayMarkerColor: Color = Color.White,
    contentDescription: String? = null,
    content: @Composable () -> Unit,
) {
    val reducedMotion = LocalReducedMotion.current
    val layoutDirection = LocalLayoutDirection.current
    val geometry = mahinRingGeometry(layoutDirection)
    val targetProgress = progressFraction.coerceIn(0f, 1f)
    val duration =
        if (reducedMotion) {
            0
        } else {
            mahinMotionDurationMs(MahinMotionDuration.NORMAL_MS)
        }
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(targetProgress, reducedMotion, duration) {
        if (reducedMotion || duration == 0) {
            animatedProgress.snapTo(targetProgress)
        } else {
            animatedProgress.animateTo(targetProgress, animationSpec = tween(durationMillis = duration))
        }
    }
    val semanticsModifier =
        if (contentDescription != null) {
            Modifier.clearAndSetSemantics { this.contentDescription = contentDescription }
        } else {
            Modifier
        }
    Box(
        modifier =
            modifier
                .size(ringSize)
                .then(semanticsModifier)
                .testTag("cycle_progress_ring"),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size
            val side = canvasSize.minDimension
            val stroke = side * 0.09f
            val diameter = side - stroke
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            val arcSize = Size(diameter, diameter)
            val sweepTotal = geometry.sweepTotal
            val startAngle = geometry.startAngle
            if (trackColor.alpha > 0f) {
                drawArc(
                    color = trackColor,
                    startAngle = startAngle,
                    sweepAngle = sweepTotal,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
            arcs.forEach { arc ->
                val segmentSweep = (arc.endFraction - arc.startFraction).coerceAtLeast(0f) * sweepTotal
                val arcStart = startAngle + arc.startFraction * sweepTotal
                val pathEffect =
                    if (arc.style == MahinRingArcStyle.Dashed) {
                        PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    } else {
                        null
                    }
                drawArc(
                    color = arc.color,
                    startAngle = arcStart,
                    sweepAngle = segmentSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style =
                        Stroke(
                            width = stroke,
                            cap = StrokeCap.Round,
                            pathEffect = pathEffect,
                        ),
                )
            }
            val fillColor = progressColor
            if (fillColor != null && animatedProgress.value > 0f) {
                drawArc(
                    color = fillColor.copy(alpha = 0.35f),
                    startAngle = startAngle,
                    sweepAngle = sweepTotal * animatedProgress.value,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke * 0.55f, cap = StrokeCap.Round),
                )
            }
            todayMarkerFraction?.let { fraction ->
                val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                val markerPoint = mahinRingTodayMarkerPoint(side, geometry, fraction)
                val markerRadius = stroke * 0.55f
                drawCircle(
                    color = todayMarkerColor,
                    radius = markerRadius,
                    center = markerPoint,
                )
            }
        }
        content()
    }
}
