package dev.mahin.android.insights

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.billing.BillingProductIds
import dev.mahin.core.designsystem.component.MahinPaywallCallbacks
import dev.mahin.core.designsystem.component.MahinPaywallSheet
import dev.mahin.core.designsystem.component.MahinPaywallState

@Composable
fun CycleInsightsScreen(
    modifier: Modifier = Modifier,
    viewModel: CycleInsightsViewModel = hiltViewModel(),
    onOpenHistory: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity

    CycleInsightsScreenContent(
        state = state,
        modifier = modifier,
        onUnlockPremium = viewModel::openPaywall,
        onOpenHistory = onOpenHistory,
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
