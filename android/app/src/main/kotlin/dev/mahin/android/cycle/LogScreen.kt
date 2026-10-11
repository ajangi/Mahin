package dev.mahin.android.cycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.EntryPointAccessors
import dev.mahin.android.navigation.AppNavigationEntryPoint
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.privacy.SensitiveScreenProtection
import dev.mahin.android.privacy.SensitiveScreenViewModel

@Composable
fun LogScreen(
    modifier: Modifier = Modifier,
    viewModel: LogViewModel = hiltViewModel(),
    sensitiveScreenViewModel: SensitiveScreenViewModel = hiltViewModel(),
) {
    SensitiveScreenProtection(enabled = sensitiveScreenViewModel.blockScreenshotsEnabled())
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current.applicationContext
    val dayEditorCoordinator =
        remember(context) {
            EntryPointAccessors.fromApplication(context, AppNavigationEntryPoint::class.java).dayEditorCoordinator()
        }
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
                onOpenDayEditor = {
                    val date = state.converter.toGregorian(state.selectedJalali)
                    dayEditorCoordinator.open(date)
                },
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
