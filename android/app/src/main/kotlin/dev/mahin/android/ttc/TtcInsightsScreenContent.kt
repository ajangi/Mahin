package dev.mahin.android.ttc

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.android.cycle.CycleFormatters
import dev.mahin.android.cycle.confidenceLabel
import dev.mahin.core.database.entity.TtcDayLogEntity
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.component.MahinLoadingState
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PregnancyTestResult
import dev.mahin.domain.fertility.FertilityInsightResult

/** Stateless TTC insights for tests and [TtcInsightsScreen]. */
@Composable
internal fun TtcInsightsScreenContent(
    state: TtcInsightsContentState,
    onOpenHistory: (() -> Unit)? = null,
) {
    when {
        state.isLoading -> MahinLoadingState(modifier = state.modifier.fillMaxSize())
        !state.isTtcMode || state.insight == null ->
            MahinEmptyState(
                title = stringResource(R.string.ttc_insights_empty_title),
                body = stringResource(R.string.ttc_insights_empty_body),
                modifier = state.modifier.padding(MahinSpacing.md),
            )
        else -> TtcInsightsLoadedContent(state = state, onOpenHistory = onOpenHistory)
    }
}

@Composable
private fun TtcInsightsLoadedContent(
    state: TtcInsightsContentState,
    onOpenHistory: (() -> Unit)?,
) {
    val insight = state.insight ?: return
    val resources = LocalContext.current.resources
    val chartSummary = BbtChartA11y.summary(resources, state.bbtPoints)
    val disclaimer = stringResource(R.string.ttc_fertility_safety_disclaimer)
    LazyColumn(
        modifier =
            state.modifier
                .fillMaxSize()
                .padding(MahinSpacing.md)
                .testTag("ttc_insights_list"),
    ) {
        item(key = "title") {
            Text(
                text = stringResource(R.string.ttc_insights_title),
                style = mahinTextStyle(MahinTypographyRole.TitleLarge),
            )
        }
        item(key = "estimate") {
            FertilityEstimateCard(insight = insight)
        }
        item(key = "chart_title") {
            Text(
                text = stringResource(R.string.ttc_bbt_chart_title),
                style = mahinTextStyle(MahinTypographyRole.Label),
                modifier = Modifier.padding(top = MahinSpacing.md),
            )
        }
        item(key = "chart") {
            TtcInsightsChartSection(state.bbtPoints, chartSummary)
        }
        item(key = "timeline_title") {
            Text(
                text = stringResource(R.string.ttc_timeline_title),
                style = mahinTextStyle(MahinTypographyRole.Label),
                modifier = Modifier.padding(top = MahinSpacing.md),
            )
        }
        if (state.timeline.isEmpty()) {
            item(key = "timeline_empty") {
                Text(
                    text = stringResource(R.string.ttc_timeline_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(state.timeline, key = { it.id }) { entry ->
                TtcTimelineRow(
                    entry = entry,
                    showIntercourse = state.intercourseLoggingEnabled,
                )
            }
        }
        item(key = "disclaimer") {
            Text(
                text = disclaimer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MahinSpacing.md),
            )
        }
        if (onOpenHistory != null) {
            item(key = "history") {
                OutlinedButton(
                    onClick = onOpenHistory,
                    modifier = Modifier.padding(bottom = MahinSpacing.lg),
                ) {
                    Text(stringResource(R.string.cycle_insights_open_history))
                }
            }
        }
    }
}

@Composable
private fun TtcInsightsChartSection(
    bbtPoints: List<BbtChartPoint>,
    chartSummary: String,
) {
    if (bbtPoints.isEmpty()) {
        Text(
            text = stringResource(R.string.ttc_bbt_chart_empty),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        BbtChart(
            points = bbtPoints,
            contentDescription = stringResource(R.string.ttc_bbt_chart_a11y, chartSummary),
        )
    }
}

@Composable
private fun FertilityEstimateCard(insight: FertilityInsightResult) {
    val prediction = insight.cyclePrediction
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = MahinSpacing.sm)) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(MahinSpacing.md)) {
            Text(
                text = stringResource(R.string.ttc_fertility_estimate_title),
                style = mahinTextStyle(MahinTypographyRole.Label),
            )
            Text(
                text = stringResource(R.string.today_confidence, confidenceLabel(prediction.confidence)),
                style = MaterialTheme.typography.bodyMedium,
            )
            prediction.fertileWindow?.let { range ->
                Text(
                    text = stringResource(R.string.today_fertile_window, CycleFormatters.formatRange(range)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (insight.ovulationTestSurgeDates.isNotEmpty()) {
                val dates =
                    insight.ovulationTestSurgeDates
                        .map { CycleFormatters.formatLocalDate(it) }
                        .joinToString("، ")
                Text(
                    text = stringResource(R.string.ttc_logged_opk_dates, dates),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (insight.fertileEggWhiteDates.isNotEmpty()) {
                val dates =
                    insight.fertileEggWhiteDates
                        .map { CycleFormatters.formatLocalDate(it) }
                        .joinToString("، ")
                Text(
                    text = stringResource(R.string.ttc_logged_egg_white_dates, dates),
                    style = MaterialTheme.typography.bodySmall,
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
                text = stringResource(R.string.ttc_fertility_estimate_safety_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
        }
    }
}

@Composable
private fun TtcTimelineRow(
    entry: TtcDayLogEntity,
    showIntercourse: Boolean,
) {
    Card(modifier = Modifier.padding(vertical = MahinSpacing.xxs)) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(MahinSpacing.sm)) {
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
                    text = stringResource(R.string.ttc_timeline_opk, opkLabel(result)),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            entry.cervicalMucus?.let { mucus ->
                Text(
                    text = stringResource(R.string.ttc_timeline_mucus, mucusLabel(mucus)),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (showIntercourse && entry.intercourseLogged) {
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
                    text = stringResource(R.string.ttc_timeline_pregnancy_test, pregnancyTestLabel(result)),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun opkLabel(result: OvulationTestResult): String =
    when (result) {
        OvulationTestResult.NEGATIVE -> stringResource(R.string.opk_negative)
        OvulationTestResult.POSITIVE -> stringResource(R.string.opk_positive)
        OvulationTestResult.PEAK -> stringResource(R.string.opk_peak)
        OvulationTestResult.UNCLEAR -> stringResource(R.string.opk_unclear)
    }

@Composable
private fun mucusLabel(type: CervicalMucusType): String =
    when (type) {
        CervicalMucusType.DRY -> stringResource(R.string.mucus_dry)
        CervicalMucusType.STICKY -> stringResource(R.string.mucus_sticky)
        CervicalMucusType.CREAMY -> stringResource(R.string.mucus_creamy)
        CervicalMucusType.WATERY -> stringResource(R.string.mucus_watery)
        CervicalMucusType.EGG_WHITE -> stringResource(R.string.mucus_egg_white)
        CervicalMucusType.OTHER -> stringResource(R.string.mucus_other)
    }

@Composable
private fun pregnancyTestLabel(result: PregnancyTestResult): String =
    when (result) {
        PregnancyTestResult.NEGATIVE -> stringResource(R.string.pregnancy_test_negative)
        PregnancyTestResult.POSITIVE -> stringResource(R.string.pregnancy_test_positive)
        PregnancyTestResult.UNCLEAR -> stringResource(R.string.pregnancy_test_unclear)
    }
