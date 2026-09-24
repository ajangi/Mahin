package dev.mahin.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ttcPrivacyDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mahin_ttc_privacy",
)

@Singleton
class TtcPrivacyPreferencesRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.ttcPrivacyDataStore

        fun observeIntercourseLoggingEnabled(): Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[TtcPreferenceKeys.intercourseLoggingEnabled] ?: false
            }

        suspend fun setIntercourseLoggingEnabled(enabled: Boolean) {
            dataStore.edit { prefs ->
                prefs[TtcPreferenceKeys.intercourseLoggingEnabled] = enabled
            }
        }
    }
