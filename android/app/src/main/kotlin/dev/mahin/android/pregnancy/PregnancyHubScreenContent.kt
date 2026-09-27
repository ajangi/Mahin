package dev.mahin.android.pregnancy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.android.cycle.CycleFormatters
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.core.model.PregnancyAppointmentType
import dev.mahin.core.model.PregnancyOutcome
import dev.mahin.domain.pregnancy.PregnancyStatusSnapshot

data class PregnancyHubContentState(
    val isLoading: Boolean,
    val isPregnantMode: Boolean,
    val postTransition: Boolean,
    val status: PregnancyStatusSnapshot?,
    val kickSessionActive: Boolean,
    val kickCount: Int,
    val kickElapsedSeconds: Long,
    val contractionSessionActive: Boolean,
    val contractionInProgress: Boolean,
    val contractionElapsedSeconds: Long,
    val appointments: List<PregnancyAppointmentListItem>,
    val newAppointmentTitle: String,
    val newAppointmentType: PregnancyAppointmentType,
    val newAppointmentJalali: dev.mahin.core.datetime.JalaliDate,
    val selectedOutcome: PregnancyOutcome?,
    val wantsSupportContent: Boolean,
    val suppressCelebratoryNotifications: Boolean,
    val weeklyCmsTitle: String? = null,
    val weeklyCmsSummary: String? = null,
)

data class PregnancyAppointmentListItem(
    val title: String,
    val whenLabel: String,
)

data class PregnancyHubActions(
    val onNewAppointmentTitleChange: (String) -> Unit,
    val onNewAppointmentTypeSelected: (PregnancyAppointmentType) -> Unit,
    val onNewAppointmentDateSelected: (dev.mahin.core.datetime.JalaliDate) -> Unit,
    val onAddAppointment: () -> Unit,
    val onStartKickSession: () -> Unit,
    val onStopKickSession: () -> Unit,
    val onRecordKick: () -> Unit,
    val onStartContractionSession: () -> Unit,
    val onEndContractionSession: () -> Unit,
    val onToggleContraction: () -> Unit,
    val onOutcomeSelected: (PregnancyOutcome) -> Unit,
    val onSupportContentToggle: (Boolean) -> Unit,
    val onSaveOutcome: () -> Unit,
    val onResumeCycle: () -> Unit,
    val onResumeTtc: () -> Unit,
)

@Composable
fun PregnancyHubScreenContent(
    state: PregnancyHubContentState,
    actions: PregnancyHubActions,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> {
            Text(
                text = stringResource(R.string.pregnancy_hub_title),
                modifier = modifier.padding(MahinSpacing.md),
            )
        }
        state.postTransition -> {
            PostTransitionContent(actions = actions, modifier = modifier)
        }
        !state.isPregnantMode || state.status == null -> {
            MahinEmptyState(
                title = stringResource(R.string.pregnancy_hub_empty_title),
                body = stringResource(R.string.pregnancy_hub_empty_body),
                modifier = modifier.padding(MahinSpacing.md),
            )
        }
        else -> {
            ActivePregnancyHub(
                state = state,
                actions = actions,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ActivePregnancyHub(
    state: PregnancyHubContentState,
    actions: PregnancyHubActions,
    modifier: Modifier = Modifier,
) {
    val status = state.status ?: return
    val disclaimer = stringResource(R.string.pregnancy_hub_safety_disclaimer)
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("pregnancy_hub_list")
                .padding(MahinSpacing.md),
        verticalArrangement = Arrangement.spacedBy(MahinSpacing.sm),
    ) {
        item {
            Text(
                text = stringResource(R.string.pregnancy_hub_title),
                style = mahinTextStyle(MahinTypographyRole.TitleLarge),
            )
        }
        item {
            PregnancyStatusCard(status = status)
        }
        if (!state.suppressCelebratoryNotifications) {
            item {
                WeekPlaceholderCard(
                    weekNumber = status.displayWeekNumber,
                    cmsTitle = state.weeklyCmsTitle,
                    cmsSummary = state.weeklyCmsSummary,
                )
            }
        }
        item {
            KickCounterSection(state = state, actions = actions)
        }
        item {
            ContractionTimerSection(state = state, actions = actions)
        }
        item {
            AppointmentsSection(state = state, actions = actions)
        }
        item {
            OutcomeSection(state = state, actions = actions)
        }
        item {
            Text(
                text = disclaimer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MahinSpacing.md),
            )
        }
    }
}

@Composable
private fun PregnancyStatusCard(status: PregnancyStatusSnapshot) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
            val ga = status.gestationalAge
            val gaLabel =
                "${PregnancyFormatters.formatInteger(ga.weeks)} هفته و " +
                    "${PregnancyFormatters.formatInteger(ga.days)} روز"
            Text(
                text = stringResource(R.string.pregnancy_gestational_age, gaLabel),
                style = mahinTextStyle(MahinTypographyRole.NumericDisplay),
            )
            Text(
                text = stringResource(pregnancyTrimesterLabelRes(status.trimester)),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text =
                    stringResource(
                        R.string.pregnancy_edd_countdown,
                        PregnancyFormatters.formatLong(status.daysUntilEdd),
                    ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text =
                    stringResource(
                        R.string.pregnancy_effective_edd,
                        CycleFormatters.formatLocalDate(status.dating.effectiveEddDate),
                    ),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.pregnancy_dating_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeekPlaceholderCard(
    weekNumber: Int,
    cmsTitle: String? = null,
    cmsSummary: String? = null,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
            Text(
                text =
                    stringResource(
                        R.string.pregnancy_week_card_title,
                        PregnancyFormatters.formatInteger(weekNumber),
                    ),
                style = mahinTextStyle(MahinTypographyRole.Label),
            )
            if (cmsSummary != null) {
                cmsTitle?.let {
                    Text(text = it, style = MaterialTheme.typography.titleSmall)
                }
                Text(
                    text = cmsSummary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Text(
                    text = stringResource(R.string.pregnancy_week_placeholder_fetal),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.pregnancy_week_placeholder_maternal),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = MahinSpacing.xs),
                )
            }
        }
    }
}

@Composable
private fun KickCounterSection(
    state: PregnancyHubContentState,
    actions: PregnancyHubActions,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
            Text(
                text = stringResource(R.string.pregnancy_kick_section_title),
                style = mahinTextStyle(MahinTypographyRole.Label),
            )
            if (state.kickSessionActive) {
                Text(
                    text =
                        stringResource(
                            R.string.pregnancy_kick_count,
                            PregnancyFormatters.formatInteger(state.kickCount),
                        ),
                )
                MahinPrimaryButton(
                    text = stringResource(R.string.pregnancy_kick_record),
                    onClick = actions.onRecordKick,
                    modifier = Modifier.padding(top = MahinSpacing.sm),
                )
                MahinPrimaryButton(
                    text = stringResource(R.string.pregnancy_kick_stop),
                    onClick = actions.onStopKickSession,
                    modifier = Modifier.padding(top = MahinSpacing.xs),
                )
            } else {
                MahinPrimaryButton(
                    text = stringResource(R.string.pregnancy_kick_start),
                    onClick = actions.onStartKickSession,
                )
            }
            Text(
                text = stringResource(R.string.pregnancy_kick_safety),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
        }
    }
}

@Composable
private fun ContractionTimerSection(
    state: PregnancyHubContentState,
    actions: PregnancyHubActions,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
            Text(
                text = stringResource(R.string.pregnancy_contraction_section_title),
                style = mahinTextStyle(MahinTypographyRole.Label),
            )
            if (!state.contractionSessionActive) {
                MahinPrimaryButton(
                    text = stringResource(R.string.pregnancy_contraction_start_session),
                    onClick = actions.onStartContractionSession,
                )
            } else {
                if (state.contractionInProgress) {
                    Text(
                        text =
                            stringResource(
                                R.string.pregnancy_contraction_elapsed,
                                PregnancyFormatters.formatDurationSeconds(state.contractionElapsedSeconds),
                            ),
                    )
                    MahinPrimaryButton(
                        text = stringResource(R.string.pregnancy_contraction_stop),
                        onClick = actions.onToggleContraction,
                        modifier = Modifier.padding(top = MahinSpacing.sm),
                    )
                } else {
                    MahinPrimaryButton(
                        text = stringResource(R.string.pregnancy_contraction_start),
                        onClick = actions.onToggleContraction,
                    )
                }
                MahinPrimaryButton(
                    text = stringResource(R.string.pregnancy_contraction_end_session),
                    onClick = actions.onEndContractionSession,
                    modifier = Modifier.padding(top = MahinSpacing.xs),
                )
            }
            Text(
                text = stringResource(R.string.pregnancy_contraction_safety),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
        }
    }
}

@Composable
private fun AppointmentsSection(
    state: PregnancyHubContentState,
    actions: PregnancyHubActions,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
            Text(
                text = stringResource(R.string.pregnancy_appointments_title),
                style = mahinTextStyle(MahinTypographyRole.Label),
            )
            state.appointments.forEach { item ->
                Text(
                    text = "${item.title} — ${item.whenLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = MahinSpacing.xxs),
                )
            }
            OutlinedTextField(
                value = state.newAppointmentTitle,
                onValueChange = actions.onNewAppointmentTitleChange,
                label = { Text(stringResource(R.string.pregnancy_appointment_title_label)) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = MahinSpacing.sm),
            )
            PregnancyAppointmentType.entries.forEach { type ->
                FilterChip(
                    selected = state.newAppointmentType == type,
                    onClick = { actions.onNewAppointmentTypeSelected(type) },
                    label = { Text(appointmentTypeLabel(type)) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = MahinSpacing.xxs),
                )
            }
            MahinJalaliDatePicker(
                selectedDate = state.newAppointmentJalali,
                onDateSelected = actions.onNewAppointmentDateSelected,
                converter = PersianCivilDateConverter,
                initialVisibleMonth = state.newAppointmentJalali,
            )
            MahinPrimaryButton(
                text = stringResource(R.string.pregnancy_appointment_save),
                onClick = actions.onAddAppointment,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
        }
    }
}

@Composable
private fun OutcomeSection(
    state: PregnancyHubContentState,
    actions: PregnancyHubActions,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
            Text(
                text = stringResource(R.string.pregnancy_outcome_title),
                style = mahinTextStyle(MahinTypographyRole.Label),
            )
            Text(
                text = stringResource(R.string.pregnancy_outcome_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutcomeChip(
                selected = state.selectedOutcome == PregnancyOutcome.LIVE_BIRTH,
                label = stringResource(R.string.pregnancy_outcome_live_birth),
                onClick = { actions.onOutcomeSelected(PregnancyOutcome.LIVE_BIRTH) },
            )
            OutcomeChip(
                selected = state.selectedOutcome == PregnancyOutcome.PREGNANCY_LOSS,
                label = stringResource(R.string.pregnancy_outcome_loss),
                onClick = { actions.onOutcomeSelected(PregnancyOutcome.PREGNANCY_LOSS) },
            )
            OutcomeChip(
                selected = state.selectedOutcome == PregnancyOutcome.TERMINATION,
                label = stringResource(R.string.pregnancy_outcome_termination),
                onClick = { actions.onOutcomeSelected(PregnancyOutcome.TERMINATION) },
            )
            OutcomeChip(
                selected = state.selectedOutcome == PregnancyOutcome.ENDED_UNSPECIFIED,
                label = stringResource(R.string.pregnancy_outcome_unspecified),
                onClick = { actions.onOutcomeSelected(PregnancyOutcome.ENDED_UNSPECIFIED) },
            )
            RowSwitch(
                label = stringResource(R.string.pregnancy_outcome_support_opt_in),
                checked = state.wantsSupportContent,
                onCheckedChange = actions.onSupportContentToggle,
            )
            MahinPrimaryButton(
                text = stringResource(R.string.pregnancy_outcome_save),
                onClick = actions.onSaveOutcome,
                enabled = state.selectedOutcome != null,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
        }
    }
}

@Composable
private fun OutcomeChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = MahinSpacing.xxs),
    )
}

@Composable
private fun RowSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.padding(top = MahinSpacing.sm)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PostTransitionContent(
    actions: PregnancyHubActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(MahinSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.pregnancy_post_transition_hint),
            style = MaterialTheme.typography.bodyMedium,
        )
        MahinPrimaryButton(
            text = stringResource(R.string.pregnancy_resume_cycle),
            onClick = actions.onResumeCycle,
            modifier = Modifier.padding(top = MahinSpacing.md),
        )
        MahinPrimaryButton(
            text = stringResource(R.string.pregnancy_resume_ttc),
            onClick = actions.onResumeTtc,
            modifier = Modifier.padding(top = MahinSpacing.sm),
        )
    }
}

@Composable
private fun appointmentTypeLabel(type: PregnancyAppointmentType): String =
    when (type) {
        PregnancyAppointmentType.CLINICIAN_VISIT -> stringResource(R.string.pregnancy_appointment_type_clinician)
        PregnancyAppointmentType.ULTRASOUND -> stringResource(R.string.pregnancy_appointment_type_ultrasound)
        PregnancyAppointmentType.LABORATORY -> stringResource(R.string.pregnancy_appointment_type_lab)
        PregnancyAppointmentType.SCREENING -> stringResource(R.string.pregnancy_appointment_type_screening)
        PregnancyAppointmentType.VACCINATION -> stringResource(R.string.pregnancy_appointment_type_vaccination)
        PregnancyAppointmentType.CUSTOM -> stringResource(R.string.pregnancy_appointment_type_custom)
    }
