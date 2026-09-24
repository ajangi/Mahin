package dev.mahin.android.cycle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinEmptyState
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
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(MahinSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.today_title),
            style = mahinTextStyle(MahinTypographyRole.TitleLarge),
        )
        val dashboard = state.dashboard
        if (dashboard == null) {
            MahinEmptyState(
                title = stringResource(R.string.today_empty_title),
                body = stringResource(R.string.today_empty_body),
            )
            return@Column
        }
        TodayPredictionCard(prediction = dashboard.prediction)
        if (dashboard.onPeriodToday) {
            Text(
                text = stringResource(R.string.today_on_period),
                style = mahinTextStyle(MahinTypographyRole.Label),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (dashboard.todayLog != null) {
            Text(
                text = stringResource(R.string.today_has_daily_log),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
        }
        Spacer(modifier = Modifier.height(MahinSpacing.md))
        ReproductiveModeCard(
            currentMode = state.reproductiveMode,
            onModeSelected = viewModel::onReproductiveModeSelected,
        )
        if (state.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE) {
            Text(
                text = stringResource(R.string.today_ttc_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
        }
    }
}

@Composable
private fun TodayPredictionCard(prediction: CyclePredictionResult) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.padding(vertical = MahinSpacing.sm),
    ) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
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
}
