package dev.mahin.backend.entitlement

import dev.mahin.backend.entitlement.persistence.EntitlementGrantEntity
import dev.mahin.backend.entitlement.persistence.EntitlementGrantRepository
import dev.mahin.backend.entitlement.persistence.PlaySubscriptionRecordEntity
import dev.mahin.backend.entitlement.persistence.PlaySubscriptionRecordRepository
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EntitlementService(
    private val grantRepository: EntitlementGrantRepository,
    private val playSubscriptionRecordRepository: PlaySubscriptionRecordRepository,
    private val purchaseVerifier: GooglePlayPurchaseVerifier,
) {
    @Transactional(readOnly = true)
    fun currentEntitlement(userId: UUID): EntitlementResponse {
        val now = Instant.now()
        val tier = resolveActiveTier(userId, now)
        return EntitlementResponse(
            tier = tier,
            expiresAt = activeExpiry(userId, now),
            features = EntitlementFeatureCatalog.featuresFor(tier),
            verifiedAt = now,
        )
    }

    @Transactional
    fun verifyGooglePlayPurchase(
        userId: UUID,
        productId: String,
        purchaseToken: String,
    ): GooglePlayBillingResponse {
        val verification = purchaseVerifier.verify(productId, purchaseToken)
        val now = Instant.now()
        val tokenHash = sha256Hex(purchaseToken)
        val existing = playSubscriptionRecordRepository.findByUserIdAndPurchaseTokenHash(userId, tokenHash)
        val record =
            playSubscriptionRecordRepository.save(
                PlaySubscriptionRecordEntity(
                    id = existing?.id ?: UUID.randomUUID(),
                    userId = userId,
                    productId = productId,
                    purchaseTokenHash = tokenHash,
                    subscriptionState = verification.state,
                    expiresAt = verification.expiresAt,
                    acknowledgedAt = existing?.acknowledgedAt ?: now,
                    updatedAt = now,
                ),
            )
        if (verification.accepted && verification.tier != null && verification.tier != EntitlementTier.FREE) {
            revokeActiveGrants(userId, now)
            grantRepository.save(
                EntitlementGrantEntity(
                    id = UUID.randomUUID(),
                    userId = userId,
                    tier = verification.tier.name,
                    source = "google_play",
                    startsAt = now,
                    expiresAt = verification.expiresAt,
                    createdAt = now,
                    revokedAt = null,
                ),
            )
        } else if (!verification.accepted) {
            revokeActiveGrants(userId, now)
        }
        val tier = resolveActiveTier(userId, now)
        return GooglePlayBillingResponse(
            tier = tier,
            expiresAt = record.expiresAt,
        )
    }

    @Transactional
    fun restoreGooglePlayPurchases(
        userId: UUID,
        purchases: List<GooglePlayVerifyRequest>,
    ): GooglePlayBillingResponse {
        var best: GooglePlayBillingResponse? = null
        purchases.forEach { purchase ->
            val result = verifyGooglePlayPurchase(userId, purchase.productId, purchase.purchaseToken)
            if (best == null || EntitlementFeatureCatalog.isPremium(result.tier)) {
                best = result
            }
        }
        return best ?: GooglePlayBillingResponse(EntitlementTier.FREE, null)
    }

    private fun resolveActiveTier(
        userId: UUID,
        now: Instant,
    ): EntitlementTier {
        val grants = grantRepository.findActiveGrants(userId, now)
        val tier =
            grants
                .mapNotNull { runCatching { EntitlementTier.valueOf(it.tier) }.getOrNull() }
                .maxByOrNull { tierRank(it) }
        return tier ?: EntitlementTier.FREE
    }

    private fun activeExpiry(
        userId: UUID,
        now: Instant,
    ): Instant? = grantRepository.findActiveGrants(userId, now).mapNotNull { it.expiresAt }.maxOrNull()

    private fun revokeActiveGrants(
        userId: UUID,
        now: Instant,
    ) {
        grantRepository.findActiveGrants(userId, now).forEach { grant ->
            grantRepository.save(
                EntitlementGrantEntity(
                    id = grant.id,
                    userId = grant.userId,
                    tier = grant.tier,
                    source = grant.source,
                    startsAt = grant.startsAt,
                    expiresAt = grant.expiresAt,
                    createdAt = grant.createdAt,
                    revokedAt = now,
                ),
            )
        }
    }

    private fun tierRank(tier: EntitlementTier): Int =
        when (tier) {
            EntitlementTier.FREE -> 0
            EntitlementTier.PREMIUM_MONTHLY -> 1
            EntitlementTier.PREMIUM_ANNUAL -> 2
        }

    companion object {
        fun sha256Hex(value: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(value.toByteArray(StandardCharsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
