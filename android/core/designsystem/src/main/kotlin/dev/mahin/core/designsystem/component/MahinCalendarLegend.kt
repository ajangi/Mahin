package dev.mahin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.mahin.core.designsystem.LocalMahinExtendedColors
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.R

@Composable
fun MahinCalendarLegend(modifier: Modifier = Modifier) {
    val extended = LocalMahinExtendedColors.current
    val description = stringResource(R.string.ds_calendar_legend_content_description)
    Column(
        modifier =
            modifier
                .semantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(MahinSpacing.xs),
    ) {
        LegendRow(
            color = extended.healthPeriod.copy(alpha = 0.45f),
            label = stringResource(R.string.ds_calendar_legend_period_logged),
        )
        LegendRow(
            color = extended.healthPeriod.copy(alpha = 0.2f),
            label = stringResource(R.string.ds_calendar_legend_period_predicted),
        )
        LegendRow(
            color = extended.healthFertility.copy(alpha = 0.25f),
            label = stringResource(R.string.ds_calendar_legend_fertile),
        )
        Text(
            text = stringResource(R.string.ds_calendar_legend_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MahinSpacing.xs),
        )
    }
}

@Composable
private fun LegendRow(
    color: androidx.compose.ui.graphics.Color,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm),
    ) {
        Box(
            modifier =
                Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
