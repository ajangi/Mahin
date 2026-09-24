package dev.mahin.android.ttc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import dev.mahin.android.cycle.CycleFormatters
import dev.mahin.android.cycle.confidenceLabel
import dev.mahin.core.database.entity.TtcDayLogEntity
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.domain.fertility.FertilityInsightResult

/** Stateless TTC insights for tests and [TtcInsightsScreen]. */
@Composable
internal fun TtcInsightsScreenContent(
    insight: FertilityInsightResult?,
    timeline: List<TtcDayLogEntity>,
    bbtPoints: List<BbtChartPoint>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(MahinSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.ttc_insights_title),
            style = mahinTextStyle(MahinTypographyRole.TitleLarge),
        )
        if (insight == null) {
            MahinEmptyState(
                title = stringResource(R.string.ttc_insights_empty_title),
                body = stringResource(R.string.ttc_insights_empty_body),
            )
            return
        }
        FertilityEstimateCard(insight = insight)
        Spacer(modifier = Modifier.height(MahinSpacing.md))
        Text(
            text = stringResource(R.string.ttc_bbt_chart_title),
            style = mahinTextStyle(MahinTypographyRole.Label),
        )
        if (bbtPoints.isEmpty()) {
            Text(
                text = stringResource(R.string.ttc_bbt_chart_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            BbtChart(
                points = bbtPoints,
                contentDescription = stringResource(R.string.ttc_bbt_chart_a11y),
            )
        }
        Spacer(modifier = Modifier.height(MahinSpacing.md))
        Text(
            text = stringResource(R.string.ttc_timeline_title),
            style = mahinTextStyle(MahinTypographyRole.Label),
        )
        if (timeline.isEmpty()) {
            Text(
                text = stringResource(R.string.ttc_timeline_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) {
                items(timeline, key = { it.id }) { entry ->
                    TtcTimelineRow(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun FertilityEstimateCard(insight: FertilityInsightResult) {
    val prediction = insight.cyclePrediction
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
            Text(
                text = stringResource(R.string.ttc_fertility_estimate_title),
                style = mahinTextStyle(MahinTypographyRole.Label),
            )
            Text(
                text = stringResource(R.string.today_confidence, confidenceLabel(prediction.confidence)),
                style = MaterialTheme.typography.bodyMedium,
            )
            insight.highlightedFertileWindow?.let { range ->
                Text(
                    text = stringResource(R.string.ttc_highlighted_fertile_window, CycleFormatters.formatRange(range)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } ?: prediction.fertileWindow?.let { range ->
                Text(
                    text = stringResource(R.string.today_fertile_window, CycleFormatters.formatRange(range)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (insight.signalAlignedWithEstimate) {
                Text(
                    text = stringResource(R.string.ttc_signal_aligned),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            insight.bbtShiftSuggestedDate?.let { shiftDate ->
                Text(
                    text =
                        stringResource(
                            R.string.ttc_bbt_shift_hint,
                            CycleFormatters.formatLocalDate(shiftDate),
                        ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = stringResource(R.string.ttc_fertility_safety_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
        }
    }
}

@Composable
private fun TtcTimelineRow(entry: TtcDayLogEntity) {
    Card(modifier = Modifier.padding(vertical = MahinSpacing.xxs)) {
        Column(modifier = Modifier.padding(MahinSpacing.sm)) {
            Text(
                text = CycleFormatters.formatLocalDate(entry.logDate),
                style = MaterialTheme.typography.bodyLarge,
            )
            entry.bbtCelsius?.let { temp ->
                Text(
                    text = stringResource(R.string.ttc_timeline_bbt, temp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            entry.ovulationTestResult?.let { result ->
                Text(
                    text = stringResource(R.string.ttc_timeline_opk, opkLabel(result.name)),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            entry.cervicalMucus?.let { mucus ->
                Text(
                    text = stringResource(R.string.ttc_timeline_mucus, mucusLabel(mucus.name)),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (entry.intercourseLogged) {
                val protection =
                    when (entry.intercourseProtected) {
                        true -> stringResource(R.string.ttc_intercourse_protected)
                        false -> stringResource(R.string.ttc_intercourse_unprotected)
                        null -> stringResource(R.string.ttc_intercourse_unspecified)
                    }
                Text(
                    text = stringResource(R.string.ttc_timeline_intercourse, protection),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            entry.pregnancyTestResult?.let { result ->
                Text(
                    text = stringResource(R.string.ttc_timeline_pregnancy_test, pregnancyTestLabel(result.name)),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun opkLabel(name: String): String =
    when (name) {
        "NEGATIVE" -> stringResource(R.string.opk_negative)
        "POSITIVE" -> stringResource(R.string.opk_positive)
        "PEAK" -> stringResource(R.string.opk_peak)
        else -> stringResource(R.string.opk_unclear)
    }

@Composable
private fun mucusLabel(name: String): String =
    when (name) {
        "DRY" -> stringResource(R.string.mucus_dry)
        "STICKY" -> stringResource(R.string.mucus_sticky)
        "CREAMY" -> stringResource(R.string.mucus_creamy)
        "WATERY" -> stringResource(R.string.mucus_watery)
        else -> stringResource(R.string.mucus_egg_white)
    }

@Composable
private fun pregnancyTestLabel(name: String): String =
    when (name) {
        "NEGATIVE" -> stringResource(R.string.pregnancy_test_negative)
        "POSITIVE" -> stringResource(R.string.pregnancy_test_positive)
        else -> stringResource(R.string.pregnancy_test_unclear)
    }
