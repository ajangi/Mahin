package dev.mahin.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
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

        suspend fun clearCachedArticles() {
            dataStore.edit { it.remove(BOOKMARK_IDS) }
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

        companion object {
            private val PUBLICATION_REVISION = longPreferencesKey("publication_revision")
            private val BOOKMARK_IDS = stringSetPreferencesKey("local_bookmark_ids")
        }
    }
