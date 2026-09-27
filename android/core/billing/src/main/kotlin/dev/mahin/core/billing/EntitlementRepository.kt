package dev.mahin.core.billing

import dev.mahin.core.datastore.AccountSessionRepository
import dev.mahin.core.datastore.CachedEntitlement
import dev.mahin.core.datastore.SubscriptionPreferencesRepository
import dev.mahin.core.datastore.SubscriptionPreferencesRepository.Companion.SOURCE_GOOGLE_PLAY
import dev.mahin.core.datastore.SubscriptionPreferencesRepository.Companion.SOURCE_SERVER
import dev.mahin.domain.subscription.EntitlementRules
import dev.mahin.domain.subscription.EntitlementSnapshot
import dev.mahin.domain.subscription.EntitlementSource
import dev.mahin.domain.subscription.EntitlementTier
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class EntitlementRepository
    @Inject
    constructor(
        private val subscriptionPreferencesRepository: SubscriptionPreferencesRepository,
        private val accountSessionRepository: AccountSessionRepository,
        private val entitlementApi: EntitlementApi,
    ) {
        val entitlement: Flow<EntitlementSnapshot> =
            subscriptionPreferencesRepository.cachedEntitlement.map { cached ->
                EntitlementSnapshot(
                    tier =
                        runCatching { EntitlementTier.valueOf(cached.tierName) }
                            .getOrDefault(EntitlementTier.FREE),
                    expiresAtEpochMs = cached.expiresAtEpochMs,
                    source =
                        runCatching { EntitlementSource.valueOf(cached.sourceName) }
                            .getOrDefault(EntitlementSource.LOCAL_DEFAULT),
                    syncedAtEpochMs = cached.syncedAtEpochMs,
                )
            }

        val hasPremiumAccess: Flow<Boolean> = entitlement.map { EntitlementRules.hasPremiumAccess(it) }

        suspend fun applyPlayPurchase(receipt: BillingPurchaseReceipt) {
            val expiresAt = Instant.now().plusSeconds(DEFAULT_PLAY_CACHE_SECONDS).toEpochMilli()
            subscriptionPreferencesRepository.saveEntitlement(
                CachedEntitlement(
                    tierName = receipt.tier.name,
                    expiresAtEpochMs = expiresAt,
                    sourceName = SOURCE_GOOGLE_PLAY,
                    syncedAtEpochMs = null,
                ),
            )
            syncWithServerIfPossible(receipt)
        }

        suspend fun restoreFromPlay(receipts: List<BillingPurchaseReceipt>) {
            receipts.forEach { applyPlayPurchase(it) }
            val token = accountSessionRepository.accessToken.first()
            if (token != null && receipts.isNotEmpty()) {
                runCatching {
                    entitlementApi.restorePurchases(
                        authorization = bearer(token),
                        body =
                            GooglePlayRestoreRequest(
                                purchases =
                                    receipts.map {
                                        GooglePlayVerifyRequest(
                                            productId = it.productId,
                                            purchaseToken = it.purchaseToken,
                                        )
                                    },
                            ),
                    )
                }.onSuccess { response ->
                    persistServerTier(response.tier, response.expiresAt)
                }
            }
        }

        suspend fun refreshFromServer() {
            syncWithServerIfPossible(null)
        }

        suspend fun clearLocalEntitlement() {
            subscriptionPreferencesRepository.clear()
        }

        private suspend fun syncWithServerIfPossible(receipt: BillingPurchaseReceipt?) {
            val token = accountSessionRepository.accessToken.first() ?: return
            if (receipt != null) {
                runCatching {
                    entitlementApi.verifyPurchase(
                        authorization = bearer(token),
                        body =
                            GooglePlayVerifyRequest(
                                productId = receipt.productId,
                                purchaseToken = receipt.purchaseToken,
                            ),
                    )
                }.onSuccess { response ->
                    persistServerTier(response.tier, response.expiresAt)
                }
            } else {
                runCatching {
                    entitlementApi.currentEntitlement(bearer(token))
                }.onSuccess { response ->
                    persistServerTier(response.tier, response.expiresAt)
                }
            }
        }

        private suspend fun persistServerTier(
            tierName: String,
            expiresAtIso: String?,
        ) {
            val tier = runCatching { EntitlementTier.valueOf(tierName) }.getOrDefault(EntitlementTier.FREE)
            val expiresAt =
                expiresAtIso?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }
            subscriptionPreferencesRepository.saveEntitlement(
                CachedEntitlement(
                    tierName = tier.name,
                    expiresAtEpochMs = expiresAt,
                    sourceName = SOURCE_SERVER,
                    syncedAtEpochMs = System.currentTimeMillis(),
                ),
            )
        }

        private fun bearer(accessToken: String): String = "Bearer $accessToken"

        companion object {
            private const val DEFAULT_PLAY_CACHE_SECONDS = 30L * 24 * 3600
        }
    }
