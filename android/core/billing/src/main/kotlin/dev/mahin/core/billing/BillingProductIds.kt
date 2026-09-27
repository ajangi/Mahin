package dev.mahin.core.billing

object BillingProductIds {
    const val PREMIUM_MONTHLY = "mahin_premium_monthly"
    const val PREMIUM_ANNUAL = "mahin_premium_annual"

    val subscriptionSkus = listOf(PREMIUM_MONTHLY, PREMIUM_ANNUAL)
}
