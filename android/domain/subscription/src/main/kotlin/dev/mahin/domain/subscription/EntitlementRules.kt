package dev.mahin.domain.subscription

import java.time.Instant

object EntitlementRules {
    fun hasPremiumAccess(snapshot: EntitlementSnapshot): Boolean {
        if (snapshot.tier == EntitlementTier.FREE) return false
        val expiresAt = snapshot.expiresAtEpochMs
        if (expiresAt != null && expiresAt <= Instant.now().toEpochMilli()) {
            return false
        }
        return true
    }

    fun canUse(
        snapshot: EntitlementSnapshot,
        feature: PremiumFeature,
    ): Boolean =
        when (feature) {
            PremiumFeature.CYCLE_ADVANCED_TRENDS,
            PremiumFeature.CYCLE_CORRELATION_OBSERVATIONS,
            PremiumFeature.EXPORT_EXTENDED_LAYOUT,
            -> hasPremiumAccess(snapshot)
        }

    fun merge(
        localPlay: EntitlementSnapshot?,
        server: EntitlementSnapshot?,
    ): EntitlementSnapshot {
        val candidates = listOfNotNull(localPlay, server).filter { hasPremiumAccess(it) || it.tier == EntitlementTier.FREE }
        if (candidates.isEmpty()) {
            return EntitlementSnapshot(EntitlementTier.FREE, null, EntitlementSource.LOCAL_DEFAULT, null)
        }
        return candidates.maxBy { tierRank(it.tier) }
    }

    private fun tierRank(tier: EntitlementTier): Int =
        when (tier) {
            EntitlementTier.FREE -> 0
            EntitlementTier.PREMIUM_MONTHLY -> 1
            EntitlementTier.PREMIUM_ANNUAL -> 2
        }
}
