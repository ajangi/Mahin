package dev.mahin.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.subscriptionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mahin_subscription",
)

/** Persisted entitlement fields (opaque strings; map to domain in `:core:billing`). */
data class CachedEntitlement(
    val tierName: String,
    val expiresAtEpochMs: Long?,
    val sourceName: String,
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
                CachedEntitlement(
                    tierName = prefs[SubscriptionPreferenceKeys.tier] ?: TIER_FREE,
                    expiresAtEpochMs = prefs[SubscriptionPreferenceKeys.expiresAtEpochMs],
                    sourceName = prefs[SubscriptionPreferenceKeys.source] ?: SOURCE_LOCAL_DEFAULT,
                    syncedAtEpochMs = prefs[SubscriptionPreferenceKeys.syncedAtEpochMs],
                )
            }

        suspend fun saveEntitlement(entitlement: CachedEntitlement) {
            dataStore.edit { prefs ->
                prefs[SubscriptionPreferenceKeys.tier] = entitlement.tierName
                if (entitlement.expiresAtEpochMs != null) {
                    prefs[SubscriptionPreferenceKeys.expiresAtEpochMs] = entitlement.expiresAtEpochMs
                } else {
                    prefs.remove(SubscriptionPreferenceKeys.expiresAtEpochMs)
                }
                prefs[SubscriptionPreferenceKeys.source] = entitlement.sourceName
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

        companion object {
            const val TIER_FREE = "FREE"
            const val SOURCE_LOCAL_DEFAULT = "LOCAL_DEFAULT"
            const val SOURCE_GOOGLE_PLAY = "GOOGLE_PLAY"
            const val SOURCE_SERVER = "SERVER"
        }
    }

private object SubscriptionPreferenceKeys {
    val tier = stringPreferencesKey("tier")
    val expiresAtEpochMs = longPreferencesKey("expires_at_epoch_ms")
    val source = stringPreferencesKey("source")
    val syncedAtEpochMs = longPreferencesKey("synced_at_epoch_ms")
}
