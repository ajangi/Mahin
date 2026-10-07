package dev.mahin.android.pregnancy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.core.model.PregnancyAppointmentType

data class PregnancyAppointmentsFormState(
    val appointments: List<PregnancyAppointmentListItem>,
    val newAppointmentTitle: String,
    val newAppointmentType: PregnancyAppointmentType,
    val newAppointmentJalali: JalaliDate,
)

data class PregnancyAppointmentsActions(
    val onNewAppointmentTitleChange: (String) -> Unit,
    val onNewAppointmentTypeSelected: (PregnancyAppointmentType) -> Unit,
    val onNewAppointmentDateSelected: (JalaliDate) -> Unit,
    val onAddAppointment: () -> Unit,
)

@Composable
fun PregnancyAppointmentsSection(
    state: PregnancyAppointmentsFormState,
    actions: PregnancyAppointmentsActions,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
            Text(
                text = stringResource(R.string.pregnancy_appointments_title),
                style = mahinTextStyle(MahinTypographyRole.Label),
            )
            state.appointments.forEach { item ->
                Text(
                    text = "${item.title} — ${item.whenLabel}",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
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
internal fun appointmentTypeLabel(type: PregnancyAppointmentType): String =
    when (type) {
        PregnancyAppointmentType.CLINICIAN_VISIT -> stringResource(R.string.pregnancy_appointment_type_clinician)
        PregnancyAppointmentType.ULTRASOUND -> stringResource(R.string.pregnancy_appointment_type_ultrasound)
        PregnancyAppointmentType.LABORATORY -> stringResource(R.string.pregnancy_appointment_type_lab)
        PregnancyAppointmentType.SCREENING -> stringResource(R.string.pregnancy_appointment_type_screening)
        PregnancyAppointmentType.VACCINATION -> stringResource(R.string.pregnancy_appointment_type_vaccination)
        PregnancyAppointmentType.CUSTOM -> stringResource(R.string.pregnancy_appointment_type_custom)
    }
