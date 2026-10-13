package dev.mahin.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.contentCacheStore by preferencesDataStore(name = "mahin_content_cache")

@Singleton
class ContentCachePreferencesRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore: DataStore<Preferences> = context.contentCacheStore

        suspend fun getPublicationRevision(): Long = dataStore.data.map { it[PUBLICATION_REVISION] ?: 0L }.first()

        suspend fun setPublicationRevision(revision: Long) {
            dataStore.edit { it[PUBLICATION_REVISION] = revision }
        }

        /** Clears remote article payload cache only; local bookmark ids are preserved. */
        suspend fun clearCachedArticles() {
            dataStore.edit { prefs ->
                prefs.remove(CACHED_ARTICLES_JSON)
            }
        }

        suspend fun localBookmarkIds(): Set<String> = dataStore.data.map { it[BOOKMARK_IDS] ?: emptySet() }.first()

        suspend fun toggleLocalBookmark(documentId: String) {
            dataStore.edit { prefs ->
                val current = prefs[BOOKMARK_IDS] ?: emptySet()
                prefs[BOOKMARK_IDS] =
                    if (documentId in current) {
                        current - documentId
                    } else {
                        current + documentId
                    }
            }
        }

        suspend fun setCachedDailyTipEnvelopeJson(payload: String) {
            dataStore.edit { prefs ->
                prefs[CACHED_DAILY_TIP_JSON] = payload
            }
        }

        suspend fun cachedDailyTipEnvelopeJson(): String? =
            dataStore.data.map { it[CACHED_DAILY_TIP_JSON] }.first()

        suspend fun clearCachedDailyTip() {
            dataStore.edit { prefs ->
                prefs.remove(CACHED_DAILY_TIP_JSON)
            }
        }

        suspend fun dismissDailyTip(
            tipId: String,
            localDateIso: String,
        ) {
            dataStore.edit { prefs ->
                prefs[DISMISSED_DAILY_TIP_ID] = tipId
                prefs[DISMISSED_DAILY_TIP_DATE] = localDateIso
            }
        }

        suspend fun isDailyTipDismissed(
            tipId: String,
            localDateIso: String,
        ): Boolean =
            dataStore.data
                .map { prefs ->
                    prefs[DISMISSED_DAILY_TIP_ID] == tipId && prefs[DISMISSED_DAILY_TIP_DATE] == localDateIso
                }.first()

        companion object {
            private val PUBLICATION_REVISION = longPreferencesKey("publication_revision")
            private val BOOKMARK_IDS = stringSetPreferencesKey("local_bookmark_ids")
            private val CACHED_ARTICLES_JSON = stringPreferencesKey("cached_articles_json")
            private val CACHED_DAILY_TIP_JSON = stringPreferencesKey("cached_daily_tip_json")
            private val DISMISSED_DAILY_TIP_ID = stringPreferencesKey("dismissed_daily_tip_id")
            private val DISMISSED_DAILY_TIP_DATE = stringPreferencesKey("dismissed_daily_tip_date")
        }
    }
