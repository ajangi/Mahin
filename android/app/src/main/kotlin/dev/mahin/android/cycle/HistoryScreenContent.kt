package dev.mahin.android.cycle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.mahinTextStyle

/** Stateless history list for tests and [HistoryScreen]. */
@Composable
internal fun HistoryScreenContent(
    periods: List<PeriodRecordEntity>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(MahinSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.history_title),
            style = mahinTextStyle(MahinTypographyRole.TitleLarge),
        )
        if (periods.isEmpty()) {
            MahinEmptyState(
                title = stringResource(R.string.history_empty_title),
                body = stringResource(R.string.history_empty_body),
            )
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) {
                items(periods, key = { it.id }) { record ->
                    HistoryPeriodCard(record = record)
                }
            }
        }
    }
}

@Composable
private fun HistoryPeriodCard(record: PeriodRecordEntity) {
    Card(modifier = Modifier.padding(vertical = MahinSpacing.xs)) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
            val end = record.endDate
            val range =
                if (end == null) {
                    CycleFormatters.formatLocalDate(record.startDate)
                } else {
                    "${CycleFormatters.formatLocalDate(record.startDate)} – " +
                        CycleFormatters.formatLocalDate(end)
                }
            Text(text = range, style = MaterialTheme.typography.bodyLarge)
            record.note?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
