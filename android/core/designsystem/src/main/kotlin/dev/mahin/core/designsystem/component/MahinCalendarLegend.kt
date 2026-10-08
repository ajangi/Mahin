package dev.mahin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.R
import dev.mahin.core.designsystem.mahinMinimumTouchTarget

@Suppress("LongMethod")
@Composable
fun MahinCalendarLegend(
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    onToggleExpanded: (() -> Unit)? = null,
) {
    val description = stringResource(R.string.ds_calendar_legend_content_description)
    val toggleLabel = stringResource(R.string.ds_calendar_legend_title)
    val expandedStateDescription = stringResource(R.string.ds_calendar_legend_state_expanded)
    val collapsedStateDescription = stringResource(R.string.ds_calendar_legend_state_collapsed)
    Column(
        modifier =
            modifier
                .semantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(MahinSpacing.xs),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (onToggleExpanded != null) {
                            Modifier
                                .mahinMinimumTouchTarget()
                                .clickable(onClick = onToggleExpanded)
                                .semantics {
                                    role = Role.Button
                                    contentDescription = toggleLabel
                                    stateDescription =
                                        if (expanded) {
                                            expandedStateDescription
                                        } else {
                                            collapsedStateDescription
                                        }
                                }.testTag("calendar_legend_toggle")
                        } else {
                            Modifier
                        },
                    ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.ds_calendar_legend_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (onToggleExpanded != null) {
                Text(
                    text =
                        stringResource(
                            if (expanded) {
                                R.string.ds_calendar_legend_collapse
                            } else {
                                R.string.ds_calendar_legend_expand
                            },
                        ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (expanded) {
            LegendRow(
                color = MahinCalendarMarkerTints.periodLogged(),
                label = stringResource(R.string.ds_calendar_legend_period_logged),
            )
            LegendRow(
                color = MahinCalendarMarkerTints.periodPredictedFill(),
                label = stringResource(R.string.ds_calendar_legend_period_predicted),
                outline = true,
            )
            LegendRow(
                color = MahinCalendarMarkerTints.fertileWindow(),
                label = stringResource(R.string.ds_calendar_legend_fertile),
            )
            LegendRow(
                color = MahinCalendarMarkerTints.estimatedOvulation(),
                label = stringResource(R.string.ds_calendar_legend_ovulation),
                ovulationMarker = true,
            )
            Text(
                text = stringResource(R.string.ds_calendar_legend_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MahinSpacing.xs),
            )
        }
    }
}

@Composable
private fun LegendRow(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    outline: Boolean = false,
    ovulationMarker: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm),
    ) {
        Box(
            modifier =
                Modifier
                    .size(14.dp)
                    .clip(if (ovulationMarker) RoundedCornerShape(1.dp) else CircleShape)
                    .then(
                        if (outline) {
                            Modifier.border(1.dp, MahinCalendarMarkerTints.periodPredictedBorder(), CircleShape)
                        } else {
                            Modifier
                        },
                    ).background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
