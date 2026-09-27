package dev.mahin.android.premium

import android.app.Activity
import dev.mahin.core.billing.BillingAdapter
import dev.mahin.core.billing.BillingProductIds
import dev.mahin.core.billing.EntitlementRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumBillingCoordinator
    @Inject
    constructor(
        private val billingAdapter: BillingAdapter,
        private val entitlementRepository: EntitlementRepository,
    ) {
        suspend fun warmUp() {
            billingAdapter.startConnection()
            billingAdapter.refreshProducts()
        }

        suspend fun purchaseMonthly(activity: Activity): Boolean {
            val receipt =
                billingAdapter.launchSubscriptionPurchase(
                    activityHost = activity,
                    productId = BillingProductIds.PREMIUM_MONTHLY,
                ) ?: return false
            entitlementRepository.applyPlayPurchase(receipt)
            return true
        }

        suspend fun purchaseAnnual(activity: Activity): Boolean {
            val receipt =
                billingAdapter.launchSubscriptionPurchase(
                    activityHost = activity,
                    productId = BillingProductIds.PREMIUM_ANNUAL,
                ) ?: return false
            entitlementRepository.applyPlayPurchase(receipt)
            return true
        }

        suspend fun restorePurchases() {
            val receipts = billingAdapter.restorePurchases()
            entitlementRepository.restoreFromPlay(receipts)
        }
    }
