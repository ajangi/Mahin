package dev.mahin.android.cycle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.core.model.PeriodFlowLevel

@Composable
fun LogScreen(
    modifier: Modifier = Modifier,
    viewModel: LogViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(MahinSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.log_title),
            style = mahinTextStyle(MahinTypographyRole.TitleLarge),
        )
        MahinJalaliDatePicker(
            selectedDate = state.selectedJalali,
            onDateSelected = viewModel::onDateSelected,
            converter = state.converter,
            initialVisibleMonth = state.selectedJalali,
        )
        Text(
            text = stringResource(R.string.log_period_section),
            style = mahinTextStyle(MahinTypographyRole.Label),
            modifier = Modifier.padding(top = MahinSpacing.md),
        )
        FilterChip(
            selected = state.loggingPeriod,
            onClick = viewModel::toggleLoggingPeriod,
            label = { Text(stringResource(R.string.log_period_toggle)) },
        )
        if (state.loggingPeriod) {
            PeriodFlowLevel.entries.forEach { level ->
                FilterChip(
                    selected = state.flowLevel == level,
                    onClick = { viewModel.onFlowLevelSelected(level) },
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
                onClick = { viewModel.toggleSymptom(tag) },
                label = { Text(tag) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = MahinSpacing.xxs),
            )
        }
        OutlinedTextField(
            value = state.note,
            onValueChange = viewModel::onNoteChange,
            label = { Text(stringResource(R.string.log_note_label)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(MahinSpacing.lg))
        MahinPrimaryButton(
            text = stringResource(R.string.log_save),
            onClick = viewModel::save,
            enabled = !state.saving,
        )
        if (state.saved) {
            Text(
                text = stringResource(R.string.log_saved_confirmation),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
        }
    }
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
