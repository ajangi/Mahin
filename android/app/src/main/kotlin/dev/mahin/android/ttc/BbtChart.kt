package dev.mahin.android.ttc

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.mahin.core.designsystem.MahinThemeTokens

@Composable
fun BbtChart(
    points: List<BbtChartPoint>,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val sorted = points.sortedBy { it.date }
    if (sorted.isEmpty()) return
    val minTemp = sorted.minOf { it.celsius } - 0.1
    val maxTemp = sorted.maxOf { it.celsius } + 0.1
    val lineColor = MahinThemeTokens.extendedColors.healthFertility
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(180.dp)
                .semantics { this.contentDescription = contentDescription },
    ) {
        val width = size.width
        val height = size.height
        val horizontalPadding = 16f
        val verticalPadding = 12f
        val chartWidth = width - horizontalPadding * 2
        val chartHeight = height - verticalPadding * 2

        repeat(3) { index ->
            val y = verticalPadding + chartHeight * index / 2f
            drawLine(
                color = gridColor,
                start = Offset(horizontalPadding, y),
                end = Offset(width - horizontalPadding, y),
                strokeWidth = 1f,
            )
        }

        val path = Path()
        sorted.forEachIndexed { index, point ->
            val x =
                horizontalPadding +
                    if (sorted.size == 1) {
                        chartWidth / 2f
                    } else {
                        chartWidth * index / (sorted.size - 1).toFloat()
                    }
            val ratio = ((point.celsius - minTemp) / (maxTemp - minTemp)).toFloat().coerceIn(0f, 1f)
            val y = verticalPadding + chartHeight * (1f - ratio)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            drawCircle(
                color = lineColor,
                radius = 4f,
                center = Offset(x, y),
            )
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = 3f))
    }
}
