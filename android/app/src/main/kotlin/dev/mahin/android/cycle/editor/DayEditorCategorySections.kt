package dev.mahin.android.cycle.editor

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.mahin.android.R
import dev.mahin.android.cycle.BbtFieldError
import dev.mahin.android.cycle.MahinLogTagIconMapper
import dev.mahin.core.datetime.PersianDigits
import dev.mahin.core.designsystem.LocalReducedMotion
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinIconToggleCell
import dev.mahin.core.designsystem.component.MahinSectionLabel
import dev.mahin.core.designsystem.icon.MahinIcon
import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.LogEditorCategory
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PainLocation
import dev.mahin.core.model.PeriodFlowLevel
import dev.mahin.core.model.PregnancyTestResult

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DayEditorPainSection(
    state: DayEditorUiState,
    confirmPulseKey: String?,
    onSetPain: (PainLocation, Int) -> Unit,
    onAdjustSeverity: (Int) -> Unit,
    onPainNoteChange: (String) -> Unit,
) {
    MahinSectionLabel(text = stringResource(R.string.day_editor_pain_section))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
        PainLocation.entries.forEach { location ->
            FilterChip(
                selected = state.painLocation == location,
                onClick = { onSetPain(location, state.painSeverity ?: 5) },
                label = { Text(painLocationLabel(location)) },
                modifier = Modifier.testTag("day_editor_pain_location_${location.name}"),
            )
        }
    }
    val severity = state.painSeverity ?: 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = { onAdjustSeverity((severity - 1).coerceAtLeast(0)) },
            modifier = Modifier.testTag("day_editor_pain_decrease"),
        ) {
            Text(text = "−", style = MaterialTheme.typography.titleLarge)
        }
        DayEditorPulseText(
            pulse = confirmPulseKey == "pain",
            text =
                stringResource(
                    R.string.day_editor_pain_severity,
                    PersianDigits.format(severity),
                ),
            modifier = Modifier.testTag("day_editor_pain_severity"),
        )
        IconButton(
            onClick = { onAdjustSeverity((severity + 1).coerceAtMost(10)) },
            modifier = Modifier.testTag("day_editor_pain_increase"),
        ) {
            Text(text = "+", style = MaterialTheme.typography.titleLarge)
        }
    }
    OutlinedTextField(
        value = state.painNote,
        onValueChange = onPainNoteChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("day_editor_pain_note_field"),
        label = { Text(stringResource(R.string.day_editor_pain_note_label)) },
        minLines = 1,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DayEditorPeriodFlowSection(
    periodLogged: Boolean,
    flowLevel: PeriodFlowLevel?,
    onSetFlowLevel: (PeriodFlowLevel) -> Unit,
) {
    if (!periodLogged) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
        PeriodFlowLevel.entries.forEach { level ->
            FilterChip(
                selected = flowLevel == level,
                onClick = { onSetFlowLevel(level) },
                label = { Text(flowLevelLabel(level)) },
                modifier = Modifier.testTag("day_editor_flow_${level.name}"),
            )
        }
    }
}

@Composable
internal fun DayEditorBbtSection(
    bbtInput: String,
    bbtError: BbtFieldError?,
    onBbtChange: (String) -> Unit,
) {
    MahinSectionLabel(text = stringResource(R.string.log_bbt_label))
    OutlinedTextField(
        value = bbtInput,
        onValueChange = onBbtChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("day_editor_bbt_field"),
        label = { Text(stringResource(R.string.log_bbt_label)) },
        isError = bbtError != null,
        supportingText = {
            when (bbtError) {
                BbtFieldError.UNPARSEABLE -> Text(stringResource(R.string.log_bbt_error_unparseable))
                BbtFieldError.OUT_OF_RANGE -> Text(stringResource(R.string.log_bbt_error_range))
                null -> Unit
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DayEditorCustomSymptomChipsSection(
    labels: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    if (labels.isEmpty()) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
        labels.forEach { tag ->
            FilterChip(
                selected = tag in selected,
                onClick = { onToggle(tag) },
                label = { Text(tag) },
                modifier = Modifier.testTag("day_editor_custom_symptom_chip_$tag"),
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

@Composable
internal fun DayEditorNotesSection(
    note: String,
    onNoteChange: (String) -> Unit,
) {
    MahinSectionLabel(text = stringResource(R.string.log_note_label))
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("day_editor_note_field"),
        label = { Text(stringResource(R.string.day_editor_notes_hint)) },
        minLines = 2,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DayEditorDischargeSection(
    selected: CervicalMucusType?,
    onSelect: (CervicalMucusType) -> Unit,
) {
    MahinSectionLabel(text = stringResource(R.string.log_mucus_section))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
        CervicalMucusType.entries.forEach { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(type) },
                label = { Text(mucusLabel(type)) },
                modifier = Modifier.testTag("day_editor_mucus_${type.name}"),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DayEditorTtcTestsSection(
    ovulationTest: OvulationTestResult?,
    pregnancyTest: PregnancyTestResult?,
    onOvulationTest: (OvulationTestResult) -> Unit,
    onPregnancyTest: (PregnancyTestResult) -> Unit,
) {
    Text(
        text = stringResource(R.string.log_ttc_privacy_note),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = MahinSpacing.sm),
    )
    MahinSectionLabel(text = stringResource(R.string.log_opk_section))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
        OvulationTestResult.entries.forEach { result ->
            FilterChip(
                selected = ovulationTest == result,
                onClick = { onOvulationTest(result) },
                label = { Text(opkLabel(result)) },
                modifier = Modifier.testTag("day_editor_opk_${result.name}"),
            )
        }
    }
    MahinSectionLabel(text = stringResource(R.string.log_pregnancy_test_section))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
        PregnancyTestResult.entries.forEach { result ->
            FilterChip(
                selected = pregnancyTest == result,
                onClick = { onPregnancyTest(result) },
                label = { Text(pregnancyTestLabel(result)) },
                modifier = Modifier.testTag("day_editor_hpt_${result.name}"),
            )
        }
    }
}

@Composable
internal fun DayEditorSexualSection(
    intercourseLogged: Boolean,
    intercourseProtected: Boolean?,
    onToggleIntercourse: () -> Unit,
    onProtectedSelected: (Boolean?) -> Unit,
) {
    MahinSectionLabel(text = stringResource(R.string.day_editor_sexual_section))
    Text(
        text = stringResource(R.string.day_editor_sexual_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    FilterChip(
        selected = intercourseLogged,
        onClick = onToggleIntercourse,
        label = { Text(stringResource(R.string.log_intercourse_toggle)) },
        modifier = Modifier.testTag("day_editor_intercourse_toggle"),
    )
    if (intercourseLogged) {
        Row(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
            FilterChip(
                selected = intercourseProtected == true,
                onClick = { onProtectedSelected(true) },
                label = { Text(stringResource(R.string.ttc_intercourse_protected)) },
                modifier = Modifier.testTag("day_editor_intercourse_protected"),
            )
            FilterChip(
                selected = intercourseProtected == false,
                onClick = { onProtectedSelected(false) },
                label = { Text(stringResource(R.string.ttc_intercourse_unprotected)) },
                modifier = Modifier.testTag("day_editor_intercourse_unprotected"),
            )
        }
    }
}

@Composable
internal fun DayEditorLifestyleSection(
    category: LogEditorCategory,
    label: String,
    logged: Boolean,
    onToggle: () -> Unit,
) {
    MahinSectionLabel(text = label)
    FilterChip(
        selected = logged,
        onClick = onToggle,
        label = { Text(label) },
        modifier = Modifier.testTag("day_editor_lifestyle_${category.name.lowercase()}_toggle"),
    )
}

@Composable
internal fun DayEditorCustomSymptomSection(
    draft: String,
    onDraftChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    MahinSectionLabel(text = stringResource(R.string.day_editor_custom_symptom_label))
    OutlinedTextField(
        value = draft,
        onValueChange = onDraftChange,
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("day_editor_custom_symptom_field"),
        label = { Text(stringResource(R.string.day_editor_custom_symptom_hint)) },
    )
    TextButton(onClick = onAdd, modifier = Modifier.testTag("day_editor_custom_symptom_add")) {
        Text(stringResource(R.string.day_editor_custom_symptom_add))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DayEditorPregnancySymptomsSection(
    available: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    MahinSectionLabel(text = stringResource(R.string.log_symptoms_section))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
        available.forEach { tag ->
            FilterChip(
                selected = tag in selected,
                onClick = { onToggle(tag) },
                label = { Text(tag) },
                modifier = Modifier.testTag("day_editor_pregnancy_symptom_$tag"),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DayEditorSymptomsSection(
    state: DayEditorUiState,
    confirmPulseKey: String?,
    onToggleSymptom: (String) -> Unit,
) {
    MahinSectionLabel(text = stringResource(R.string.log_symptoms_section))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
        state.availableSymptoms.forEach { tag ->
            val selected = tag in state.symptomTags
            val pulse = confirmPulseKey == "symptom_$tag"
            DayEditorPulseBox(pulse = pulse) {
                MahinIconToggleCell(
                    label = tag,
                    selected = selected,
                    onClick = { onToggleSymptom(tag) },
                    modifier = Modifier.testTag("day_editor_symptom_$tag"),
                    icon = {
                        MahinIcon(
                            icon = MahinLogTagIconMapper.symptomIcon(tag),
                            modifier = Modifier.size(24.dp),
                        )
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DayEditorMoodSection(
    state: DayEditorUiState,
    confirmPulseKey: String?,
    onToggleMood: (String) -> Unit,
) {
    MahinSectionLabel(text = stringResource(R.string.day_editor_mood_section))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
        state.availableMoods.forEach { tag ->
            val selected = tag in state.moodTags
            val pulse = confirmPulseKey == "mood_$tag"
            DayEditorPulseBox(pulse = pulse) {
                MahinIconToggleCell(
                    label = tag,
                    selected = selected,
                    onClick = { onToggleMood(tag) },
                    modifier = Modifier.testTag("day_editor_mood_$tag"),
                    icon = {
                        MahinIcon(
                            icon = MahinLogTagIconMapper.moodIcon(tag),
                            modifier = Modifier.size(24.dp),
                        )
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DayEditorMedicationSection(
    available: List<String>,
    selected: Set<String>,
    confirmPulseKey: String?,
    onToggle: (String) -> Unit,
) {
    MahinSectionLabel(text = stringResource(R.string.day_editor_medication_section))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
        available.forEach { tag ->
            val pulse = confirmPulseKey == "medication_$tag"
            DayEditorPulseBox(pulse = pulse) {
                FilterChip(
                    selected = tag in selected,
                    onClick = { onToggle(tag) },
                    label = { Text(tag) },
                    modifier = Modifier.testTag("day_editor_medication_$tag"),
                )
            }
        }
    }
}

@Composable
@Suppress("LongParameterList")
internal fun DayEditorPregnancyVitalsSection(
    weightInput: String,
    weightError: Boolean,
    systolicInput: String,
    diastolicInput: String,
    bpError: Boolean,
    onWeightChange: (String) -> Unit,
    onSystolicChange: (String) -> Unit,
    onDiastolicChange: (String) -> Unit,
) {
    MahinSectionLabel(text = stringResource(R.string.log_pregnancy_section))
    OutlinedTextField(
        value = weightInput,
        onValueChange = onWeightChange,
        label = { Text(stringResource(R.string.log_pregnancy_weight_label)) },
        isError = weightError,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = MahinSpacing.xs)
                .testTag("day_editor_pregnancy_weight"),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm),
    ) {
        OutlinedTextField(
            value = systolicInput,
            onValueChange = onSystolicChange,
            label = { Text(stringResource(R.string.log_pregnancy_bp_systolic)) },
            isError = bpError,
            modifier =
                Modifier
                    .weight(1f)
                    .testTag("day_editor_pregnancy_bp_systolic"),
        )
        OutlinedTextField(
            value = diastolicInput,
            onValueChange = onDiastolicChange,
            label = { Text(stringResource(R.string.log_pregnancy_bp_diastolic)) },
            isError = bpError,
            modifier =
                Modifier
                    .weight(1f)
                    .testTag("day_editor_pregnancy_bp_diastolic"),
        )
    }
}

@Composable
private fun DayEditorPulseBox(
    pulse: Boolean,
    content: @Composable () -> Unit,
) {
    val reducedMotion = LocalReducedMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pulse && !reducedMotion) 1.06f else 1f,
        label = "dayEditorPulse",
    )
    androidx.compose.foundation.layout.Box(modifier = Modifier.scale(scale)) {
        content()
    }
}

@Composable
private fun DayEditorPulseText(
    pulse: Boolean,
    text: String,
    modifier: Modifier = Modifier,
) {
    val reducedMotion = LocalReducedMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pulse && !reducedMotion) 1.08f else 1f,
        label = "dayEditorPulseText",
    )
    Text(text = text, modifier = modifier.scale(scale))
}

@Composable
private fun painLocationLabel(location: PainLocation): String =
    when (location) {
        PainLocation.ABDOMEN -> stringResource(R.string.day_editor_pain_abdomen)
        PainLocation.BACK -> stringResource(R.string.day_editor_pain_back)
        PainLocation.HEAD -> stringResource(R.string.day_editor_pain_head)
        PainLocation.BREASTS -> stringResource(R.string.day_editor_pain_breasts)
        PainLocation.PELVIS -> stringResource(R.string.day_editor_pain_pelvis)
        PainLocation.LEGS -> stringResource(R.string.day_editor_pain_legs)
        PainLocation.OTHER -> stringResource(R.string.day_editor_pain_other)
    }

@Composable
private fun mucusLabel(type: CervicalMucusType): String =
    when (type) {
        CervicalMucusType.DRY -> stringResource(R.string.mucus_dry)
        CervicalMucusType.STICKY -> stringResource(R.string.mucus_sticky)
        CervicalMucusType.CREAMY -> stringResource(R.string.mucus_creamy)
        CervicalMucusType.WATERY -> stringResource(R.string.mucus_watery)
        CervicalMucusType.EGG_WHITE -> stringResource(R.string.mucus_egg_white)
        CervicalMucusType.OTHER -> stringResource(R.string.mucus_other)
    }

@Composable
private fun opkLabel(result: OvulationTestResult): String =
    when (result) {
        OvulationTestResult.NEGATIVE -> stringResource(R.string.opk_negative)
        OvulationTestResult.POSITIVE -> stringResource(R.string.opk_positive)
        OvulationTestResult.PEAK -> stringResource(R.string.opk_peak)
        OvulationTestResult.UNCLEAR -> stringResource(R.string.opk_unclear)
    }

@Composable
private fun pregnancyTestLabel(result: PregnancyTestResult): String =
    when (result) {
        PregnancyTestResult.NEGATIVE -> stringResource(R.string.pregnancy_test_negative)
        PregnancyTestResult.POSITIVE -> stringResource(R.string.pregnancy_test_positive)
        PregnancyTestResult.UNCLEAR -> stringResource(R.string.pregnancy_test_unclear)
    }
