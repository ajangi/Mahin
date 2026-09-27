package dev.mahin.backend.entitlement

import java.time.Instant

data class PlayPurchaseVerification(
    val accepted: Boolean,
    val tier: EntitlementTier?,
    val expiresAt: Instant?,
    val state: String,
)

interface GooglePlayPurchaseVerifier {
    fun verify(
        productId: String,
        purchaseToken: String,
    ): PlayPurchaseVerification
}

/**
 * Non-production verifier for integration tests and local dev.
 * Never log [purchaseToken]. Production should call Google Play Developer API.
 */
class DevGooglePlayPurchaseVerifier : GooglePlayPurchaseVerifier {
    @Suppress("ReturnCount")
    override fun verify(
        productId: String,
        purchaseToken: String,
    ): PlayPurchaseVerification {
        if (!purchaseToken.startsWith(TEST_TOKEN_PREFIX)) {
            return rejected("rejected")
        }
        val tier = tierForProduct(productId) ?: return rejected("unknown_product")
        val expiresAt =
            if (purchaseToken.contains("expired")) {
                Instant.now().minusSeconds(3600)
            } else {
                Instant.now().plusSeconds(DEFAULT_ACTIVE_SECONDS)
            }
        val active = expiresAt.isAfter(Instant.now())
        return PlayPurchaseVerification(
            accepted = active,
            tier = if (active) tier else EntitlementTier.FREE,
            expiresAt = expiresAt,
            state = if (active) "active" else "expired",
        )
    }

    private fun rejected(state: String): PlayPurchaseVerification =
        PlayPurchaseVerification(
            accepted = false,
            tier = null,
            expiresAt = null,
            state = state,
        )

    companion object {
        const val TEST_TOKEN_PREFIX = "gp-test-"
        private const val DEFAULT_ACTIVE_SECONDS = 30L * 24 * 3600
        const val PRODUCT_PREMIUM_MONTHLY = "mahin_premium_monthly"
        const val PRODUCT_PREMIUM_ANNUAL = "mahin_premium_annual"

        fun tierForProduct(productId: String): EntitlementTier? =
            when (productId) {
                PRODUCT_PREMIUM_MONTHLY -> EntitlementTier.PREMIUM_MONTHLY
                PRODUCT_PREMIUM_ANNUAL -> EntitlementTier.PREMIUM_ANNUAL
                else -> null
            }
    }
}

/**
 * Production default until Google Play Developer API verification is implemented.
 * Fails closed: never grants premium from unverified tokens.
 */
class RejectingGooglePlayPurchaseVerifier : GooglePlayPurchaseVerifier {
    override fun verify(
        productId: String,
        purchaseToken: String,
    ): PlayPurchaseVerification =
        PlayPurchaseVerification(
            accepted = false,
            tier = null,
            expiresAt = null,
            state = "unverified",
        )
}
