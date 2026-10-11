package dev.mahin.android.cycle.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.datetime.PersianDigits
import dev.mahin.core.designsystem.LocalReducedMotion
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinSectionLabel
import dev.mahin.core.model.LogEditorCategory
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
                onAdjustPainSeverity = viewModel::adjustPainSeverity,
                onNoteChange = viewModel::setNote,
                onMucusSelected = viewModel::setCervicalMucus,
                onOvulationTest = viewModel::setOvulationTest,
                onPregnancyTest = viewModel::setPregnancyTest,
                onToggleIntercourse = viewModel::toggleIntercourse,
                onIntercourseProtected = viewModel::setIntercourseProtected,
                onToggleLifestyle = viewModel::toggleLifestyleLogged,
                onCustomSymptomDraft = viewModel::setCustomSymptomDraft,
                onAddCustomSymptom = viewModel::addCustomSymptomFromDraft,
                onTogglePregnancySymptom = viewModel::togglePregnancySymptom,
                onToggleMedication = viewModel::toggleMedication,
                onPregnancyWeightChange = viewModel::onPregnancyWeightChange,
                onPregnancyBpSystolicChange = viewModel::onPregnancyBpSystolicChange,
                onPregnancyBpDiastolicChange = viewModel::onPregnancyBpDiastolicChange,
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
            onMostUsedFirstToggle = viewModel::setMostUsedFirst,
            onDisableSexualActivity = viewModel::disableSexualActivity,
            onMoveCategoryEarlier = viewModel::moveCategoryEarlier,
            onMoveCategoryLater = viewModel::moveCategoryLater,
        )
    }
}

@Composable
@Suppress("LongMethod", "LongParameterList", "CyclomaticComplexMethod")
internal fun DayEditorSheetContent(
    date: LocalDate,
    state: DayEditorUiState,
    onTogglePeriod: () -> Unit = {},
    onToggleSymptom: (String) -> Unit = {},
    onToggleMood: (String) -> Unit = {},
    onSetPain: (dev.mahin.core.model.PainLocation, Int) -> Unit = { _, _ -> },
    onAdjustPainSeverity: (Int) -> Unit = {},
    onNoteChange: (String) -> Unit = {},
    onMucusSelected: (dev.mahin.core.model.CervicalMucusType) -> Unit = {},
    onOvulationTest: (dev.mahin.core.model.OvulationTestResult) -> Unit = {},
    onPregnancyTest: (dev.mahin.core.model.PregnancyTestResult) -> Unit = {},
    onToggleIntercourse: () -> Unit = {},
    onIntercourseProtected: (Boolean?) -> Unit = {},
    onToggleLifestyle: (LogEditorCategory) -> Unit = {},
    onCustomSymptomDraft: (String) -> Unit = {},
    onAddCustomSymptom: () -> Unit = {},
    onTogglePregnancySymptom: (String) -> Unit = {},
    onToggleMedication: (String) -> Unit = {},
    onPregnancyWeightChange: (String) -> Unit = {},
    onPregnancyBpSystolicChange: (String) -> Unit = {},
    onPregnancyBpDiastolicChange: (String) -> Unit = {},
    onOpenManage: () -> Unit = {},
    onRequestSexualOptIn: () -> Unit = {},
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
                .padding(MahinSpacing.lg)
                .testTag("day_editor_scroll"),
    ) {
        Text(text = title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
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
                                    contentDescription = "period_started"
                                },
                    )
                }
                LogEditorCategory.SYMPTOMS -> {
                    DayEditorSymptomsSection(state, state.confirmPulseKey, onToggleSymptom)
                    DayEditorCustomSymptomSection(
                        draft = state.customSymptomDraft,
                        onDraftChange = onCustomSymptomDraft,
                        onAdd = onAddCustomSymptom,
                    )
                }
                LogEditorCategory.MOOD ->
                    DayEditorMoodSection(state, state.confirmPulseKey, onToggleMood)
                LogEditorCategory.PAIN ->
                    DayEditorPainSection(
                        state = state,
                        confirmPulseKey = state.confirmPulseKey,
                        onSetPain = onSetPain,
                        onAdjustSeverity = onAdjustPainSeverity,
                    )
                LogEditorCategory.DISCHARGE ->
                    DayEditorDischargeSection(state.cervicalMucus, onMucusSelected)
                LogEditorCategory.TTC_TESTS ->
                    DayEditorTtcTestsSection(
                        ovulationTest = state.ovulationTest,
                        pregnancyTest = state.pregnancyTest,
                        onOvulationTest = onOvulationTest,
                        onPregnancyTest = onPregnancyTest,
                    )
                LogEditorCategory.MEDICATION ->
                    DayEditorMedicationSection(
                        available = LogDayEditorCatalog.defaultMedications,
                        selected = state.medicationTags,
                        confirmPulseKey = state.confirmPulseKey,
                        onToggle = onToggleMedication,
                    )
                LogEditorCategory.NOTES -> DayEditorNotesSection(state.note, onNoteChange)
                LogEditorCategory.SEXUAL_ACTIVITY ->
                    DayEditorSexualSection(
                        intercourseLogged = state.intercourseLogged,
                        intercourseProtected = state.intercourseProtected,
                        onToggleIntercourse = onToggleIntercourse,
                        onProtectedSelected = onIntercourseProtected,
                    )
                LogEditorCategory.SLEEP ->
                    DayEditorLifestyleSection(
                        category = LogEditorCategory.SLEEP,
                        label = stringResource(R.string.day_editor_lifestyle_sleep),
                        logged = state.sleepLogged,
                        onToggle = { onToggleLifestyle(LogEditorCategory.SLEEP) },
                    )
                LogEditorCategory.EXERCISE ->
                    DayEditorLifestyleSection(
                        category = LogEditorCategory.EXERCISE,
                        label = stringResource(R.string.day_editor_lifestyle_exercise),
                        logged = state.exerciseLogged,
                        onToggle = { onToggleLifestyle(LogEditorCategory.EXERCISE) },
                    )
                LogEditorCategory.WATER ->
                    DayEditorLifestyleSection(
                        category = LogEditorCategory.WATER,
                        label = stringResource(R.string.day_editor_lifestyle_water),
                        logged = state.waterGlasses > 0,
                        onToggle = { onToggleLifestyle(LogEditorCategory.WATER) },
                    )
                LogEditorCategory.STRESS ->
                    DayEditorLifestyleSection(
                        category = LogEditorCategory.STRESS,
                        label = stringResource(R.string.day_editor_lifestyle_stress),
                        logged = state.stressLevel != null,
                        onToggle = { onToggleLifestyle(LogEditorCategory.STRESS) },
                    )
                LogEditorCategory.PREGNANCY_SYMPTOMS ->
                    DayEditorPregnancySymptomsSection(
                        available = state.availablePregnancySymptoms,
                        selected = state.pregnancySymptomTags,
                        onToggle = onTogglePregnancySymptom,
                    )
                LogEditorCategory.PREGNANCY_VITALS ->
                    DayEditorPregnancyVitalsSection(
                        weightInput = state.pregnancyWeightInput,
                        weightError = state.pregnancyWeightError != null,
                        systolicInput = state.pregnancyBpSystolicInput,
                        diastolicInput = state.pregnancyBpDiastolicInput,
                        bpError = state.pregnancyBpError != null,
                        onWeightChange = onPregnancyWeightChange,
                        onSystolicChange = onPregnancyBpSystolicChange,
                        onDiastolicChange = onPregnancyBpDiastolicChange,
                    )
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
private fun stringArrayResource(id: Int): Array<String> =
    androidx.compose.ui.res
        .stringArrayResource(id)
