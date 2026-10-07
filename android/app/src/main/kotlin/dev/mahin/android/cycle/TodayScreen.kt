package dev.mahin.android.cycle

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.android.pregnancy.PregnancyTodayCard
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.component.MahinScreenHeader
import dev.mahin.core.designsystem.component.MahinSurfaceCard
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.cycle.CyclePredictionResult
import dev.mahin.domain.cycle.PredictionConfidence

@Composable
fun TodayScreen(
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TodayScreenContent(
        state = state,
        modifier = modifier,
    )
}

@Composable
internal fun TodayScreenContent(
    state: TodayUiState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("today_screen_list")
                .padding(MahinSpacing.md),
    ) {
        item {
            MahinScreenHeader(
                title = stringResource(R.string.today_title),
                subtitle = stringResource(R.string.today_subtitle),
            )
        }
        val dashboard = state.dashboard
        if (dashboard == null) {
            item {
                MahinEmptyState(
                    title = stringResource(R.string.today_empty_title),
                    body = stringResource(R.string.today_empty_body),
                )
            }
        } else if (state.reproductiveMode == ReproductiveMode.PREGNANT) {
            val pregnancyStatus = state.pregnancyStatus
            if (pregnancyStatus != null) {
                item {
                    PregnancyTodayCard(
                        status = pregnancyStatus,
                        modifier = Modifier.padding(vertical = MahinSpacing.sm),
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.pregnancy_mode_active_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            item {
                TodayPredictionCard(prediction = dashboard.prediction)
            }
            if (dashboard.onPeriodToday || dashboard.todayLog != null) {
                item {
                    TodayStatusRow(
                        onPeriodToday = dashboard.onPeriodToday,
                        hasDailyLog = dashboard.todayLog != null,
                    )
                }
            }
        }
        if (state.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE) {
            item {
                Spacer(modifier = Modifier.height(MahinSpacing.md))
                Text(
                    text = stringResource(R.string.today_ttc_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TodayStatusRow(
    onPeriodToday: Boolean,
    hasDailyLog: Boolean,
) {
    MahinSurfaceCard(modifier = Modifier.padding(vertical = MahinSpacing.sm)) {
        if (onPeriodToday) {
            Text(
                text = stringResource(R.string.today_on_period),
                style = mahinTextStyle(MahinTypographyRole.Label),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (hasDailyLog) {
            val topPad = if (onPeriodToday) Modifier.padding(top = MahinSpacing.xs) else Modifier
            Text(
                text = stringResource(R.string.today_has_daily_log),
                style = MaterialTheme.typography.bodyMedium,
                modifier = topPad,
            )
        }
    }
}

@Composable
private fun TodayPredictionCard(prediction: CyclePredictionResult) {
    MahinSurfaceCard(modifier = Modifier.padding(vertical = MahinSpacing.sm)) {
        prediction.cycleDay?.let { day ->
            Text(
                text = stringResource(R.string.today_cycle_day, day),
                style = mahinTextStyle(MahinTypographyRole.NumericDisplay),
            )
        }
        Text(
            text = stringResource(R.string.today_confidence, confidenceLabel(prediction.confidence)),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (prediction.confidence == PredictionConfidence.INSUFFICIENT_DATA) {
            Text(
                text = stringResource(R.string.today_insufficient_data_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        prediction.nextPeriod?.let { range ->
            Spacer(modifier = Modifier.height(MahinSpacing.sm))
            Text(
                text = stringResource(R.string.today_next_period, CycleFormatters.formatRange(range)),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        prediction.fertileWindow?.let { range ->
            Text(
                text = stringResource(R.string.today_fertile_window, CycleFormatters.formatRange(range)),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            text = stringResource(R.string.prediction_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MahinSpacing.sm),
        )
    }
}
