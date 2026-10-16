package dev.mahin.android.pregnancy

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
import dev.mahin.domain.pregnancy.PregnancyWeightChartPoint

@Composable
fun PregnancyWeightChart(
    points: List<PregnancyWeightChartPoint>,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val sorted = points.sortedBy { it.gestationalWeek }
    if (sorted.isEmpty()) return
    val minWeight = sorted.minOf { it.weightKg } - 1.0
    val maxWeight = sorted.maxOf { it.weightKg } + 1.0
    val lineColor = MahinThemeTokens.extendedColors.healthPregnancy
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
            val minWeek = sorted.first().gestationalWeek
            val maxWeek = sorted.last().gestationalWeek
            val x =
                horizontalPadding +
                    if (maxWeek == minWeek) {
                        chartWidth / 2f
                    } else {
                        chartWidth * (point.gestationalWeek - minWeek) / (maxWeek - minWeek).toFloat()
                    }
            val ratio = ((point.weightKg - minWeight) / (maxWeight - minWeight)).toFloat().coerceIn(0f, 1f)
            val y = verticalPadding + chartHeight * (1f - ratio)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            drawCircle(color = lineColor, radius = 4f, center = Offset(x, y))
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = 3f))
    }
}
