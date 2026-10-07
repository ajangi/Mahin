package dev.mahin.android.ttc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TtcInsightsScreen(
    modifier: Modifier = Modifier,
    onOpenHistory: (() -> Unit)? = null,
    viewModel: TtcInsightsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TtcInsightsScreenContent(
        onOpenHistory = onOpenHistory,
        state =
            TtcInsightsContentState(
                isLoading = state.isLoading,
                isTtcMode = state.isTtcMode,
                intercourseLoggingEnabled = state.intercourseLoggingEnabled,
                insight = state.insight,
                timeline = state.timeline,
                bbtPoints = state.bbtPoints,
                modifier = modifier,
            ),
    )
}
