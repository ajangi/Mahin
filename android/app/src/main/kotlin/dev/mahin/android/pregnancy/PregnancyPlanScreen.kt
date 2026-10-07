package dev.mahin.android.pregnancy

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.android.cycle.CycleFormatters
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinScreenHeader
import java.time.Instant
import java.time.ZoneId

@Composable
fun PregnancyPlanScreen(
    modifier: Modifier = Modifier,
    viewModel: PregnancyHubViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val contentState =
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
            suppressCelebratoryNotifications = state.suppressCelebratoryNotifications,
        )
    val actions =
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
        )
    PregnancyPlanScreenContent(
        state = contentState,
        actions = actions,
        modifier = modifier,
    )
}

@Composable
fun PregnancyPlanScreenContent(
    state: PregnancyHubContentState,
    actions: PregnancyHubActions,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("pregnancy_plan_list")
                .padding(MahinSpacing.md),
    ) {
        item {
            MahinScreenHeader(
                title = stringResource(R.string.nav_plan),
                subtitle = stringResource(R.string.pregnancy_appointments_title),
            )
        }
        item {
            PregnancyAppointmentsSection(
                state = state,
                actions = actions,
                modifier = Modifier.padding(top = MahinSpacing.md),
            )
        }
    }
}
