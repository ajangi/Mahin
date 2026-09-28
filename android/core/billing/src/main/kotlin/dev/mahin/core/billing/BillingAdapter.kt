package dev.mahin.core.billing

import dev.mahin.domain.subscription.EntitlementTier
import kotlinx.coroutines.flow.StateFlow

data class BillingProductOffer(
    val productId: String,
    val formattedPrice: String?,
)

data class BillingPurchaseReceipt(
    val productId: String,
    val purchaseToken: String,
    val tier: EntitlementTier,
)

sealed interface BillingAdapterState {
    data object Idle : BillingAdapterState

    data object Connecting : BillingAdapterState

    data class Ready(
        val products: List<BillingProductOffer>,
    ) : BillingAdapterState

    data class Error(
        val message: String,
    ) : BillingAdapterState
}

interface BillingAdapter {
    val state: StateFlow<BillingAdapterState>

    suspend fun startConnection()

    suspend fun refreshProducts()

    suspend fun restorePurchases(): List<BillingPurchaseReceipt>

    /**
     * Returns a Play purchase token when billing completes, or null when user cancels.
     * [activityHost] must be a ComponentActivity for Play Billing flows.
     */
    suspend fun launchSubscriptionPurchase(
        activityHost: Any,
        productId: String,
    ): BillingPurchaseReceipt?
}
