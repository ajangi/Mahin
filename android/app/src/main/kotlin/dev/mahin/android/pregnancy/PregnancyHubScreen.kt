package dev.mahin.android.pregnancy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PregnancyHubScreen(
    modifier: Modifier = Modifier,
    onOpenHistory: (() -> Unit)? = null,
    onOpenCycleCalendar: (() -> Unit)? = null,
    viewModel: PregnancyHubViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PregnancyHubScreenContent(
        modifier = modifier,
        state =
            PregnancyHubContentState(
                isLoading = state.isLoading,
                isPregnantMode = state.isPregnantMode,
                postTransition = state.postTransition,
                status = state.status,
                kickSessionActive = state.kickSessionId != null,
                kickCount = state.kickCount,
                kickElapsedSeconds = state.kickElapsedSeconds,
                contractionSessionActive = state.contractionSessionId != null,
                contractionInProgress = state.openContractionEventId != null,
                contractionElapsedSeconds = state.contractionElapsedSeconds,
                selectedOutcome = state.selectedOutcome,
                wantsSupportContent = state.wantsSupportContent,
                suppressCelebratoryNotifications = state.suppressCelebratoryNotifications,
                weeklyCmsTitle = state.weeklyCmsTitle,
                weeklyCmsSummary = state.weeklyCmsSummary,
            ),
        onOpenHistory = onOpenHistory,
        onOpenCycleCalendar = onOpenCycleCalendar,
        actions =
            PregnancyHubActions(
                onStartKickSession = viewModel::startKickSession,
                onStopKickSession = viewModel::stopKickSession,
                onRecordKick = viewModel::recordKick,
                onStartContractionSession = viewModel::startContractionSession,
                onEndContractionSession = viewModel::endContractionSession,
                onToggleContraction = viewModel::toggleContraction,
                onOutcomeSelected = viewModel::onOutcomeSelected,
                onSupportContentToggle = viewModel::onSupportContentToggle,
                onSaveOutcome = viewModel::saveOutcome,
            ),
    )
}
