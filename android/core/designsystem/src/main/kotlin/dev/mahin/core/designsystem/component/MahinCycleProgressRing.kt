package dev.mahin.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
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

@Suppress("LongParameterList")
@Composable
fun MahinCycleProgressRing(
    arcs: List<MahinRingArc>,
    progressFraction: Float,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    trackColor: Color = Color.Transparent,
    contentDescription: String? = null,
    content: @Composable () -> Unit,
) {
    val reducedMotion = LocalReducedMotion.current
    val target = progressFraction.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = if (reducedMotion) target else target,
        animationSpec =
            androidx.compose.animation.core.tween(
                durationMillis = mahinMotionDurationMs(MahinMotionDuration.NORMAL_MS),
            ),
        label = "cycleRingProgress",
    )
    val semanticsModifier =
        if (contentDescription != null) {
            Modifier.semantics { this.contentDescription = contentDescription }
        } else {
            Modifier
        }
    Box(
        modifier =
            modifier
                .size(size)
                .then(semanticsModifier)
                .testTag("cycle_progress_ring"),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val side = this.size.minDimension
            val stroke = side * 0.09f
            val diameter = side - stroke
            val topLeft = Offset(stroke / 2f, stroke / 2f)
            val arcSize = Size(diameter, diameter)
            val startAngle = 135f
            val sweepTotal = 270f
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
                val sweep = (arc.endFraction - arc.startFraction).coerceAtLeast(0f) * sweepTotal * animatedProgress
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
                    sweepAngle = sweep,
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
        }
        content()
    }
}
