package dev.mahin.android.settings

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.android.assistant.AssistantSettingsScreen
import dev.mahin.android.export.DataExportScreen
import dev.mahin.android.healthconnect.HealthConnectSettingsScreen
import dev.mahin.android.notifications.NotificationSettingsScreen
import dev.mahin.android.pregnancy.PregnancyStartSheet
import dev.mahin.android.privacy.PrivacySecuritySettingsScreen
import dev.mahin.core.billing.BillingProductIds
import dev.mahin.core.designsystem.component.MahinPaywallCallbacks
import dev.mahin.core.designsystem.component.MahinPaywallSheet
import dev.mahin.core.designsystem.component.MahinPaywallState

@Composable
fun SettingsScreen(
    onNavigateUp: () -> Unit,
    onOpenHistory: () -> Unit,
    onLocalDataErased: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity
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
                        onOpenPremium = viewModel::openPaywall,
                        onOpenHistory = onOpenHistory,
                        onResumeCycle = viewModel::resumeCycleTracking,
                        onResumeTtc = viewModel::resumeTtc,
                    ),
                modifier = modifier,
            )
            MahinPaywallSheet(
                visible = state.showPaywall,
                state =
                    MahinPaywallState(
                        title = stringResource(R.string.paywall_title),
                        body = stringResource(R.string.paywall_body),
                        monthlyPriceLabel = viewModel.priceLabel(BillingProductIds.PREMIUM_MONTHLY),
                        annualPriceLabel = viewModel.priceLabel(BillingProductIds.PREMIUM_ANNUAL),
                        restoreLabel = stringResource(R.string.paywall_restore),
                        dismissLabel = stringResource(R.string.paywall_dismiss),
                    ),
                callbacks =
                    MahinPaywallCallbacks(
                        onDismiss = viewModel::dismissPaywall,
                        onSubscribeMonthly = {
                            if (activity != null) {
                                viewModel.purchaseMonthly(activity)
                            }
                        },
                        onSubscribeAnnual = {
                            if (activity != null) {
                                viewModel.purchaseAnnual(activity)
                            }
                        },
                        onRestorePurchases = viewModel::restorePurchases,
                    ),
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
