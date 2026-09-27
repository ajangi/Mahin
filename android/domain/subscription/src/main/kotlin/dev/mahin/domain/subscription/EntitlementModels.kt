package dev.mahin.domain.subscription

import java.time.Instant

enum class EntitlementTier {
    FREE,
    PREMIUM_MONTHLY,
    PREMIUM_ANNUAL,
}

enum class PremiumFeature {
    CYCLE_ADVANCED_TRENDS,
    CYCLE_CORRELATION_OBSERVATIONS,
    EXPORT_EXTENDED_LAYOUT,
}

data class EntitlementSnapshot(
    val tier: EntitlementTier,
    val expiresAtEpochMs: Long?,
    val source: EntitlementSource,
    val syncedAtEpochMs: Long?,
)

enum class EntitlementSource {
    LOCAL_DEFAULT,
    GOOGLE_PLAY,
    SERVER,
}
