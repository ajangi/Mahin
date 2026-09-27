package dev.mahin.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.R

data class MahinPaywallState(
    val title: String,
    val body: String,
    val monthlyPriceLabel: String?,
    val annualPriceLabel: String?,
    val restoreLabel: String,
    val dismissLabel: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MahinPaywallSheet(
    visible: Boolean,
    state: MahinPaywallState,
    onDismiss: () -> Unit,
    onSubscribeMonthly: () -> Unit,
    onSubscribeAnnual: () -> Unit,
    onRestorePurchases: () -> Unit,
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MahinSpacing.lg, vertical = MahinSpacing.md),
            verticalArrangement = Arrangement.spacedBy(MahinSpacing.sm),
        ) {
            Text(text = state.title, style = MaterialTheme.typography.titleLarge)
            Text(text = state.body, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onSubscribeMonthly, modifier = Modifier.fillMaxWidth()) {
                val label =
                    state.monthlyPriceLabel
                        ?: stringResource(R.string.ds_paywall_price_unavailable)
                Text(stringResource(R.string.ds_paywall_subscribe_monthly, label))
            }
            Button(onClick = onSubscribeAnnual, modifier = Modifier.fillMaxWidth()) {
                val label =
                    state.annualPriceLabel
                        ?: stringResource(R.string.ds_paywall_price_unavailable)
                Text(stringResource(R.string.ds_paywall_subscribe_annual, label))
            }
            OutlinedButton(onClick = onRestorePurchases, modifier = Modifier.fillMaxWidth()) {
                Text(state.restoreLabel)
            }
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(state.dismissLabel)
            }
        }
    }
}
