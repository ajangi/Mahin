package dev.mahin.android.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.domain.subscription.CycleInsightsResult

@Composable
internal fun CycleInsightsScreenContent(
    state: CycleInsightsUiState,
    modifier: Modifier = Modifier,
    onUnlockPremium: () -> Unit,
    onOpenHistory: (() -> Unit)?,
) {
    val insights = state.insights
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(MahinSpacing.md),
        verticalArrangement = Arrangement.spacedBy(MahinSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.cycle_insights_title),
            style = mahinTextStyle(MahinTypographyRole.TitleLarge),
        )
        if (insights == null && state.loading) {
            Text(text = stringResource(R.string.cycle_insights_loading))
            return
        }
        if (insights == null) {
            MahinEmptyState(
                title = stringResource(R.string.cycle_insights_empty_title),
                body = stringResource(R.string.cycle_insights_empty_body),
            )
            return
        }
        FreeInsightsSection(insights)
        if (state.hasPremium && insights.premium != null) {
            PremiumInsightsSection(insights)
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(MahinSpacing.md)) {
                    Text(
                        text = stringResource(R.string.cycle_insights_premium_teaser_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.cycle_insights_premium_teaser_body),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = MahinSpacing.sm),
                    )
                    OutlinedButton(
                        onClick = onUnlockPremium,
                        modifier = Modifier.padding(top = MahinSpacing.sm),
                    ) {
                        Text(stringResource(R.string.paywall_unlock))
                    }
                }
            }
        }
        Text(
            text = insights.disclaimerFa,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (onOpenHistory != null) {
            OutlinedButton(onClick = onOpenHistory) {
                Text(stringResource(R.string.cycle_insights_open_history))
            }
        }
    }
}

@Composable
private fun FreeInsightsSection(insights: CycleInsightsResult) {
    Card {
        Column(modifier = Modifier.padding(MahinSpacing.md), verticalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
            Text(text = stringResource(R.string.cycle_insights_free_heading), style = MaterialTheme.typography.titleMedium)
            insights.cycleLengthDays?.let { range ->
                Text(stringResource(R.string.cycle_insights_cycle_length_range, range.first, range.last))
            }
            insights.periodLengthDays?.let { range ->
                Text(stringResource(R.string.cycle_insights_period_length_range, range.first, range.last))
            }
            Text(stringResource(R.string.cycle_insights_completed_cycles, insights.completedCycleCount))
            if (insights.recentSymptomTimeline.isNotEmpty()) {
                Text(stringResource(R.string.cycle_insights_symptom_timeline))
                insights.recentSymptomTimeline.forEach { line ->
                    Text(text = line, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun PremiumInsightsSection(insights: CycleInsightsResult) {
    val premium = insights.premium ?: return
    Card {
        Column(modifier = Modifier.padding(MahinSpacing.md), verticalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
            Text(text = stringResource(R.string.cycle_insights_premium_heading), style = MaterialTheme.typography.titleMedium)
            Text(premium.cycleLengthTrendLabelFa)
            premium.symptomCoOccurrenceNotesFa.forEach { note ->
                Text(text = note, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
