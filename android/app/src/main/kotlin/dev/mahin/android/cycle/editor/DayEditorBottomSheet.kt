package dev.mahin.android.cycle.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.android.cycle.MahinLogTagIconMapper
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.datetime.PersianDigits
import dev.mahin.core.designsystem.LocalReducedMotion
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinIconToggleCell
import dev.mahin.core.designsystem.component.MahinSectionLabel
import dev.mahin.core.designsystem.icon.MahinIcon
import dev.mahin.core.model.LogEditorCategory
import dev.mahin.core.model.PainLocation
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEditorHost(
    session: DayEditorSession?,
    onDismiss: () -> Unit,
    viewModel: DayEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(session?.date) {
        session?.date?.let { viewModel.openDate(it) }
    }
    if (session == null) return
    val skipHalfExpanded = LocalReducedMotion.current
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = skipHalfExpanded,
        )
    val snackbarHostState = remember { SnackbarHostState() }
    val undoLabel = stringResource(R.string.day_editor_undo)
    val savedMessage = stringResource(R.string.day_editor_saved_snackbar)
    LaunchedEffect(state.undoMessage) {
        if (state.undoMessage != null) {
            val result =
                snackbarHostState.showSnackbar(
                    message = savedMessage,
                    actionLabel = undoLabel,
                )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                viewModel.undoLastChange()
            }
            viewModel.dismissUndo()
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.testTag("day_editor_sheet"),
    ) {
        Column {
            SnackbarHost(hostState = snackbarHostState)
            DayEditorSheetContent(
                date = session.date,
                state = state,
                onTogglePeriod = viewModel::togglePeriodLogged,
                onToggleSymptom = viewModel::toggleSymptom,
                onToggleMood = viewModel::toggleMood,
                onSetPain = viewModel::setPain,
                onOpenManage = viewModel::openManageCategories,
                onRequestSexualOptIn = viewModel::requestSexualOptIn,
            )
        }
    }
    if (state.showSexualOptIn) {
        DayEditorSexualOptInDialog(
            onConfirm = viewModel::confirmSexualOptIn,
            onDismiss = viewModel::dismissSexualOptIn,
        )
    }
    if (state.showManageCategories) {
        DayEditorManageCategoriesSheet(
            state = state,
            onDismiss = viewModel::closeManageCategories,
            onLifestyleToggle = viewModel::setLifestyleEnabled,
            onRequestSexualOptIn = viewModel::requestSexualOptIn,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Suppress("LongMethod")
internal fun DayEditorSheetContent(
    date: LocalDate,
    state: DayEditorUiState,
    onTogglePeriod: () -> Unit,
    onToggleSymptom: (String) -> Unit,
    onToggleMood: (String) -> Unit,
    onSetPain: (PainLocation, Int) -> Unit,
    onOpenManage: () -> Unit,
    onRequestSexualOptIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val jalali = PersianCivilDateConverter.toJalali(date)
    val monthNames = stringArrayResource(dev.mahin.core.designsystem.R.array.ds_jalali_month_names)
    val title =
        stringResource(
            R.string.day_editor_title,
            PersianDigits.format(jalali.day),
            monthNames[jalali.month - 1],
        )
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(MahinSpacing.lg),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onOpenManage, modifier = Modifier.testTag("day_editor_manage_categories")) {
            Text(text = stringResource(R.string.day_editor_manage_categories))
        }
        state.orderedCategories.forEach { category ->
            when (category) {
                LogEditorCategory.PERIOD_FLOW -> {
                    MahinSectionLabel(text = stringResource(R.string.log_period_section))
                    FilterChip(
                        selected = state.periodLogged,
                        onClick = onTogglePeriod,
                        label = { Text(stringResource(R.string.day_editor_period_started)) },
                        modifier =
                            Modifier
                                .testTag("day_editor_period_started")
                                .padding(vertical = MahinSpacing.xs)
                                .semantics {
                                    contentDescription =
                                        "period_started"
                                },
                    )
                }
                LogEditorCategory.SYMPTOMS -> {
                    MahinSectionLabel(text = stringResource(R.string.log_symptoms_section))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
                        state.availableSymptoms.forEach { tag ->
                            val selected = tag in state.symptomTags
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
                LogEditorCategory.MOOD -> {
                    MahinSectionLabel(text = stringResource(R.string.day_editor_mood_section))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
                        state.availableMoods.forEach { tag ->
                            val selected = tag in state.moodTags
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
                LogEditorCategory.PAIN -> {
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
                    Text(
                        text =
                            stringResource(
                                R.string.day_editor_pain_severity,
                                PersianDigits.format(state.painSeverity ?: 0),
                            ),
                        modifier = Modifier.testTag("day_editor_pain_severity"),
                    )
                }
                LogEditorCategory.SEXUAL_ACTIVITY -> {
                    MahinSectionLabel(text = stringResource(R.string.day_editor_sexual_section))
                    Text(text = stringResource(R.string.day_editor_sexual_hint))
                }
                LogEditorCategory.PREGNANCY_SYMPTOMS -> {
                    MahinSectionLabel(text = stringResource(R.string.log_symptoms_section))
                }
                LogEditorCategory.PREGNANCY_VITALS -> {
                    MahinSectionLabel(text = stringResource(R.string.log_pregnancy_weight_label))
                }
                else -> Unit
            }
        }
        if (!state.sexualActivityEnabled && state.orderedCategories.none { it == LogEditorCategory.SEXUAL_ACTIVITY }) {
            TextButton(onClick = onRequestSexualOptIn, modifier = Modifier.testTag("day_editor_sexual_opt_in_entry")) {
                Text(stringResource(R.string.day_editor_enable_sexual_activity))
            }
        }
    }
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
private fun stringArrayResource(id: Int): Array<String> =
    androidx.compose.ui.res
        .stringArrayResource(id)
