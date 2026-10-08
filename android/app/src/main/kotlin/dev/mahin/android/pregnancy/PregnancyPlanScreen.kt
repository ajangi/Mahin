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
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.component.MahinScreenHeader
import java.time.Instant
import java.time.ZoneId

@Composable
fun PregnancyPlanScreen(
    modifier: Modifier = Modifier,
    viewModel: PregnancyAppointmentsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listItems =
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
        }
    val formState =
        PregnancyAppointmentsFormState(
            appointments = listItems,
            newAppointmentTitle = state.newAppointmentTitle,
            newAppointmentType = state.newAppointmentType,
            newAppointmentJalali = state.newAppointmentJalali,
        )
    val formActions =
        PregnancyAppointmentsActions(
            onNewAppointmentTitleChange = viewModel::onNewAppointmentTitleChange,
            onNewAppointmentTypeSelected = viewModel::onNewAppointmentTypeSelected,
            onNewAppointmentDateSelected = viewModel::onNewAppointmentDateSelected,
            onAddAppointment = viewModel::addAppointment,
        )
    PregnancyPlanScreenContent(
        hasActivePregnancy = state.hasActivePregnancy,
        isLoading = state.isLoading,
        formState = formState,
        formActions = formActions,
        modifier = modifier,
    )
}

@Composable
fun PregnancyPlanScreenContent(
    hasActivePregnancy: Boolean,
    isLoading: Boolean,
    formState: PregnancyAppointmentsFormState,
    formActions: PregnancyAppointmentsActions,
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
        when {
            isLoading -> Unit
            !hasActivePregnancy -> {
                item {
                    MahinEmptyState(
                        title = stringResource(R.string.pregnancy_plan_empty_title),
                        body = stringResource(R.string.pregnancy_plan_empty_body),
                        modifier = Modifier.padding(top = MahinSpacing.md),
                    )
                }
            }
            else -> {
                item {
                    PregnancyAppointmentsSection(
                        state = formState,
                        actions = formActions,
                        modifier = Modifier.padding(top = MahinSpacing.md),
                    )
                }
            }
        }
    }
}
