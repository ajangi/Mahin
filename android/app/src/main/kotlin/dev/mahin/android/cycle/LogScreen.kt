package dev.mahin.android.cycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LogScreen(
    modifier: Modifier = Modifier,
    viewModel: LogViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ttcCallbacks =
        remember(viewModel) {
            TtcLogFormCallbacks(
                onBbtChange = viewModel::onBbtChange,
                onOvulationTestSelected = viewModel::onOvulationTestSelected,
                onCervicalMucusSelected = viewModel::onCervicalMucusSelected,
                onIntercourseOptInChanged = viewModel::setIntercourseLoggingEnabled,
                onIntercourseToggle = viewModel::toggleIntercourse,
                onIntercourseProtectedSelected = viewModel::onIntercourseProtectedSelected,
                onPregnancyTestSelected = viewModel::onPregnancyTestSelected,
            )
        }
    val actions =
        remember(viewModel, ttcCallbacks) {
            LogScreenActions(
                onDateSelected = viewModel::onDateSelected,
                onToggleLoggingPeriod = viewModel::toggleLoggingPeriod,
                onFlowLevelSelected = viewModel::onFlowLevelSelected,
                onToggleSymptom = viewModel::toggleSymptom,
                onNoteChange = viewModel::onNoteChange,
                onSave = viewModel::save,
                onTogglePregnancySymptom = viewModel::togglePregnancySymptom,
                onPregnancyWeightChange = viewModel::onPregnancyWeightChange,
                onPregnancyBpSystolicChange = viewModel::onPregnancyBpSystolicChange,
                onPregnancyBpDiastolicChange = viewModel::onPregnancyBpDiastolicChange,
                ttcCallbacks = ttcCallbacks,
            )
        }
    LogScreenContent(
        state = state,
        actions = actions,
        modifier = modifier,
    )
}
