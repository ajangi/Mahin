package dev.mahin.core.designsystem.component

data class MahinPaywallState(
    val title: String,
    val body: String,
    val monthlyPriceLabel: String?,
    val annualPriceLabel: String?,
    val restoreLabel: String,
    val dismissLabel: String,
)

data class MahinPaywallCallbacks(
    val onDismiss: () -> Unit,
    val onSubscribeMonthly: () -> Unit,
    val onSubscribeAnnual: () -> Unit,
    val onRestorePurchases: () -> Unit,
)
