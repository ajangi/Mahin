package dev.mahin.domain.subscription

import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test

class EntitlementRulesTest {
    @Test
    fun expiredPremiumFallsBackToFreeCapabilities() {
        val expired =
            EntitlementSnapshot(
                tier = EntitlementTier.PREMIUM_MONTHLY,
                expiresAtEpochMs = Instant.now().minusSeconds(60).toEpochMilli(),
                source = EntitlementSource.SERVER,
                syncedAtEpochMs = Instant.now().toEpochMilli(),
            )
        assertThat(EntitlementRules.hasPremiumAccess(expired)).isFalse()
        assertThat(EntitlementRules.canUse(expired, PremiumFeature.CYCLE_ADVANCED_TRENDS)).isFalse()
    }

    @Test
    fun mergePrefersHigherActiveTier() {
        val local =
            EntitlementSnapshot(
                tier = EntitlementTier.PREMIUM_MONTHLY,
                expiresAtEpochMs = Instant.now().plusSeconds(3600).toEpochMilli(),
                source = EntitlementSource.GOOGLE_PLAY,
                syncedAtEpochMs = null,
            )
        val server =
            EntitlementSnapshot(
                tier = EntitlementTier.PREMIUM_ANNUAL,
                expiresAtEpochMs = Instant.now().plusSeconds(7200).toEpochMilli(),
                source = EntitlementSource.SERVER,
                syncedAtEpochMs = Instant.now().toEpochMilli(),
            )
        val merged = EntitlementRules.merge(local, server)
        assertThat(merged.tier).isEqualTo(EntitlementTier.PREMIUM_ANNUAL)
    }
}
