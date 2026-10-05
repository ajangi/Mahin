package dev.mahin.android.cycle

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.component.MahinScreenHeader
import dev.mahin.core.designsystem.component.MahinSurfaceCard
import dev.mahin.core.designsystem.mahinTextStyle

/** Stateless history list for tests and [HistoryScreen]. */
@Composable
internal fun HistoryScreenContent(
    periods: List<PeriodRecordEntity>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("history_screen_list")
                .padding(MahinSpacing.md),
    ) {
        item {
            MahinScreenHeader(
                title = stringResource(R.string.history_title),
                subtitle = stringResource(R.string.history_subtitle),
            )
        }
        if (periods.isEmpty()) {
            item {
                MahinEmptyState(
                    title = stringResource(R.string.history_empty_title),
                    body = stringResource(R.string.history_empty_body),
                )
            }
        } else {
            items(periods, key = { it.id }) { record ->
                HistoryPeriodCard(
                    record = record,
                    modifier = Modifier.padding(vertical = MahinSpacing.xs),
                )
            }
        }
    }
}

@Composable
private fun HistoryPeriodCard(
    record: PeriodRecordEntity,
    modifier: Modifier = Modifier,
) {
    MahinSurfaceCard(modifier = modifier) {
        val end = record.endDate
        val range =
            if (end == null) {
                CycleFormatters.formatLocalDate(record.startDate)
            } else {
                "${CycleFormatters.formatLocalDate(record.startDate)} – " +
                    CycleFormatters.formatLocalDate(end)
            }
        Text(
            text = range,
            style = mahinTextStyle(MahinTypographyRole.BodyLarge),
            color = MaterialTheme.colorScheme.onSurface,
        )
        record.note?.let { note ->
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MahinSpacing.xs),
            )
        }
    }
}
