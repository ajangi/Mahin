package dev.mahin.android.pregnancy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.component.MahinScreenHeader

@Composable
fun PregnancyWeightChartScreen(
    modifier: Modifier = Modifier,
    viewModel: PregnancyWeightChartViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PregnancyWeightChartScreenContent(state = state, modifier = modifier)
}

@Composable
fun PregnancyWeightChartScreenContent(
    state: PregnancyWeightChartUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("pregnancy_weight_chart")
                .padding(MahinSpacing.md),
    ) {
        MahinScreenHeader(
            title = stringResource(R.string.pregnancy_weight_chart_title),
            subtitle = stringResource(R.string.pregnancy_weight_chart_subtitle),
        )
        when {
            state.isLoading -> Unit
            !state.hasActivePregnancy -> {
                MahinEmptyState(
                    title = stringResource(R.string.pregnancy_weight_chart_no_pregnancy_title),
                    body = stringResource(R.string.pregnancy_weight_chart_no_pregnancy_body),
                )
            }
            state.points.isEmpty() -> {
                MahinEmptyState(
                    title = stringResource(R.string.pregnancy_weight_chart_empty_title),
                    body = stringResource(R.string.pregnancy_weight_chart_empty_body),
                )
            }
            else -> {
                Text(
                    text = stringResource(R.string.pregnancy_weight_chart_axis_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PregnancyWeightChart(
                    points = state.points,
                    contentDescription = stringResource(R.string.pregnancy_weight_chart_a11y),
                    modifier = Modifier.padding(top = MahinSpacing.md),
                )
            }
        }
    }
}
