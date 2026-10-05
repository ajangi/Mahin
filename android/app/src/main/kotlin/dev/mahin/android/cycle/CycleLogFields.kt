package dev.mahin.android.cycle

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinChoiceChip
import dev.mahin.core.designsystem.component.MahinSectionLabel
import dev.mahin.core.model.PeriodFlowLevel

@Composable
internal fun CycleLogFields(
    state: LogUiState,
    actions: LogScreenActions,
) {
    MahinSectionLabel(text = stringResource(R.string.log_period_section))
    MahinChoiceChip(
        label = stringResource(R.string.log_period_toggle),
        selected = state.loggingPeriod,
        onClick = actions.onToggleLoggingPeriod,
    )
    if (state.loggingPeriod) {
        PeriodFlowLevel.entries.forEach { level ->
            MahinChoiceChip(
                label = flowLevelLabel(level),
                selected = state.flowLevel == level,
                onClick = { actions.onFlowLevelSelected(level) },
            )
        }
    }
    MahinSectionLabel(text = stringResource(R.string.log_symptoms_section))
    state.availableSymptoms.forEach { tag ->
        MahinChoiceChip(
            label = tag,
            selected = state.symptomTags.contains(tag),
            onClick = { actions.onToggleSymptom(tag) },
        )
    }
    OutlinedTextField(
        value = state.note,
        onValueChange = actions.onNoteChange,
        label = { Text(stringResource(R.string.log_note_label)) },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = MahinSpacing.sm),
    )
}

@Composable
private fun flowLevelLabel(level: PeriodFlowLevel): String =
    when (level) {
        PeriodFlowLevel.SPOTTING -> stringResource(R.string.flow_spotting)
        PeriodFlowLevel.LIGHT -> stringResource(R.string.flow_light)
        PeriodFlowLevel.MEDIUM -> stringResource(R.string.flow_medium)
        PeriodFlowLevel.HEAVY -> stringResource(R.string.flow_heavy)
        PeriodFlowLevel.VERY_HEAVY -> stringResource(R.string.flow_very_heavy)
    }
