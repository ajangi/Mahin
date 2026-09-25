package dev.mahin.android.pregnancy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.cycle.CycleFormatters
import java.time.Instant
import java.time.ZoneId

@Composable
fun PregnancyHubScreen(
    modifier: Modifier = Modifier,
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
                appointments =
                    state.appointments.map { entity ->
                        val date =
                            Instant
                                .ofEpochMilli(entity.scheduledAtEpochMs)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        PregnancyAppointmentListItem(
                            title = entity.title,
                            whenLabel = CycleFormatters.formatLocalDate(date),
                        )
                    },
                newAppointmentTitle = state.newAppointmentTitle,
                newAppointmentType = state.newAppointmentType,
                newAppointmentJalali = state.newAppointmentJalali,
                selectedOutcome = state.selectedOutcome,
                wantsSupportContent = state.wantsSupportContent,
            ),
        actions =
            PregnancyHubActions(
                onNewAppointmentTitleChange = viewModel::onNewAppointmentTitleChange,
                onNewAppointmentTypeSelected = viewModel::onNewAppointmentTypeSelected,
                onNewAppointmentDateSelected = viewModel::onNewAppointmentDateSelected,
                onAddAppointment = viewModel::addAppointment,
                onStartKickSession = viewModel::startKickSession,
                onStopKickSession = viewModel::stopKickSession,
                onRecordKick = viewModel::recordKick,
                onStartContractionSession = viewModel::startContractionSession,
                onEndContractionSession = viewModel::endContractionSession,
                onToggleContraction = viewModel::toggleContraction,
                onOutcomeSelected = viewModel::onOutcomeSelected,
                onSupportContentToggle = viewModel::onSupportContentToggle,
                onSaveOutcome = viewModel::saveOutcome,
                onResumeCycle = viewModel::resumeCycleTracking,
                onResumeTtc = viewModel::resumeTtc,
            ),
    )
}
