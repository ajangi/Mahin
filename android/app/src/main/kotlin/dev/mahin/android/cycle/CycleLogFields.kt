package dev.mahin.android.cycle

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.core.model.PeriodFlowLevel

@Composable
internal fun CycleLogFields(
    state: LogUiState,
    actions: LogScreenActions,
) {
    Text(
        text = stringResource(R.string.log_period_section),
        style = mahinTextStyle(MahinTypographyRole.Label),
        modifier = Modifier.padding(top = MahinSpacing.md),
    )
    FilterChip(
        selected = state.loggingPeriod,
        onClick = actions.onToggleLoggingPeriod,
        label = { Text(stringResource(R.string.log_period_toggle)) },
    )
    if (state.loggingPeriod) {
        PeriodFlowLevel.entries.forEach { level ->
            FilterChip(
                selected = state.flowLevel == level,
                onClick = { actions.onFlowLevelSelected(level) },
                label = { Text(flowLevelLabel(level)) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = MahinSpacing.xxs),
            )
        }
    }
    Text(
        text = stringResource(R.string.log_symptoms_section),
        style = mahinTextStyle(MahinTypographyRole.Label),
        modifier = Modifier.padding(top = MahinSpacing.md),
    )
    state.availableSymptoms.forEach { tag ->
        FilterChip(
            selected = state.symptomTags.contains(tag),
            onClick = { actions.onToggleSymptom(tag) },
            label = { Text(tag) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = MahinSpacing.xxs),
        )
    }
    OutlinedTextField(
        value = state.note,
        onValueChange = actions.onNoteChange,
        label = { Text(stringResource(R.string.log_note_label)) },
        modifier = Modifier.fillMaxWidth(),
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
