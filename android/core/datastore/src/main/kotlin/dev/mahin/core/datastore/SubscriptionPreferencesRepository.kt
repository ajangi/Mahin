package dev.mahin.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.mahin.domain.subscription.EntitlementSource
import dev.mahin.domain.subscription.EntitlementTier
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.subscriptionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mahin_subscription",
)

data class CachedEntitlement(
    val tier: EntitlementTier,
    val expiresAtEpochMs: Long?,
    val source: EntitlementSource,
    val syncedAtEpochMs: Long?,
)

@Singleton
class SubscriptionPreferencesRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.subscriptionDataStore

        val cachedEntitlement: Flow<CachedEntitlement> =
            dataStore.data.map { prefs ->
                val tierName = prefs[SubscriptionPreferenceKeys.tier] ?: EntitlementTier.FREE.name
                val tier = runCatching { EntitlementTier.valueOf(tierName) }.getOrDefault(EntitlementTier.FREE)
                val sourceName = prefs[SubscriptionPreferenceKeys.source] ?: EntitlementSource.LOCAL_DEFAULT.name
                val source = runCatching { EntitlementSource.valueOf(sourceName) }.getOrDefault(EntitlementSource.LOCAL_DEFAULT)
                CachedEntitlement(
                    tier = tier,
                    expiresAtEpochMs = prefs[SubscriptionPreferenceKeys.expiresAtEpochMs],
                    source = source,
                    syncedAtEpochMs = prefs[SubscriptionPreferenceKeys.syncedAtEpochMs],
                )
            }

        suspend fun saveEntitlement(entitlement: CachedEntitlement) {
            dataStore.edit { prefs ->
                prefs[SubscriptionPreferenceKeys.tier] = entitlement.tier.name
                if (entitlement.expiresAtEpochMs != null) {
                    prefs[SubscriptionPreferenceKeys.expiresAtEpochMs] = entitlement.expiresAtEpochMs
                } else {
                    prefs.remove(SubscriptionPreferenceKeys.expiresAtEpochMs)
                }
                prefs[SubscriptionPreferenceKeys.source] = entitlement.source.name
                if (entitlement.syncedAtEpochMs != null) {
                    prefs[SubscriptionPreferenceKeys.syncedAtEpochMs] = entitlement.syncedAtEpochMs
                } else {
                    prefs.remove(SubscriptionPreferenceKeys.syncedAtEpochMs)
                }
            }
        }

        suspend fun clear() {
            dataStore.edit { it.clear() }
        }
    }

private object SubscriptionPreferenceKeys {
    val tier = stringPreferencesKey("tier")
    val expiresAtEpochMs = longPreferencesKey("expires_at_epoch_ms")
    val source = stringPreferencesKey("source")
    val syncedAtEpochMs = longPreferencesKey("synced_at_epoch_ms")
}
