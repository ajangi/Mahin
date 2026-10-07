package dev.mahin.android.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.assistant.AssistantSettingsScreen
import dev.mahin.android.export.DataExportScreen
import dev.mahin.android.healthconnect.HealthConnectSettingsScreen
import dev.mahin.android.notifications.NotificationSettingsScreen
import dev.mahin.android.pregnancy.PregnancyStartSheet
import dev.mahin.android.privacy.PrivacySecuritySettingsScreen

@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    onOpenHistory: () -> Unit,
    onLocalDataErased: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var overlay by rememberSaveable { mutableStateOf(SettingsOverlay.NONE) }

    when (overlay) {
        SettingsOverlay.NOTIFICATIONS ->
            NotificationSettingsScreen(onNavigateUp = { overlay = SettingsOverlay.NONE })
        SettingsOverlay.PRIVACY ->
            PrivacySecuritySettingsScreen(
                onNavigateUp = { overlay = SettingsOverlay.NONE },
                onLocalDataErased = {
                    overlay = SettingsOverlay.NONE
                    onLocalDataErased()
                },
            )
        SettingsOverlay.HEALTH_CONNECT ->
            HealthConnectSettingsScreen(onNavigateUp = { overlay = SettingsOverlay.NONE })
        SettingsOverlay.ASSISTANT ->
            AssistantSettingsScreen(onNavigateUp = { overlay = SettingsOverlay.NONE })
        SettingsOverlay.DATA_EXPORT ->
            DataExportScreen(onNavigateUp = { overlay = SettingsOverlay.NONE })
        SettingsOverlay.NONE -> {
            PregnancyStartSheet(
                visible = state.showPregnancyStartSheet,
                onDismiss = viewModel::dismissPregnancyStartSheet,
                onConfirm = viewModel::confirmPregnancyStart,
            )
            SettingsScreenContent(
                state = state,
                callbacks =
                    SettingsScreenCallbacks(
                        onNavigateUp = onNavigateUp,
                        onModeSelected = viewModel::onReproductiveModeSelected,
                        onOpenNotifications = { overlay = SettingsOverlay.NOTIFICATIONS },
                        onOpenPrivacy = { overlay = SettingsOverlay.PRIVACY },
                        onOpenHealthConnect = { overlay = SettingsOverlay.HEALTH_CONNECT },
                        onOpenAssistant = { overlay = SettingsOverlay.ASSISTANT },
                        onOpenDataExport = { overlay = SettingsOverlay.DATA_EXPORT },
                        onOpenHistory = onOpenHistory,
                        onResumeCycle = viewModel::resumeCycleTracking,
                        onResumeTtc = viewModel::resumeTtc,
                    ),
                modifier = modifier,
            )
        }
    }
}

private enum class SettingsOverlay {
    NONE,
    NOTIFICATIONS,
    PRIVACY,
    HEALTH_CONNECT,
    ASSISTANT,
    DATA_EXPORT,
}
