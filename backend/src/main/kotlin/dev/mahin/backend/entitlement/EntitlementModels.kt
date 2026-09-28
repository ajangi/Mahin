package dev.mahin.backend.entitlement

import java.time.Instant

enum class EntitlementTier {
    FREE,
    PREMIUM_MONTHLY,
    PREMIUM_ANNUAL,
}

data class EntitlementResponse(
    val tier: EntitlementTier,
    val expiresAt: Instant?,
    val features: List<String>,
    val verifiedAt: Instant,
)

data class GooglePlayVerifyRequest(
    val productId: String,
    val purchaseToken: String,
)

data class GooglePlayRestoreRequest(
    val purchases: List<GooglePlayVerifyRequest>,
)

data class GooglePlayBillingResponse(
    val tier: EntitlementTier,
    val expiresAt: Instant?,
)
