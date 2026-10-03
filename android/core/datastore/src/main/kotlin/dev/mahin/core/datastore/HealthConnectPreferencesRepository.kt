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

private val Context.healthConnectDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "health_connect_preferences",
)

data class HealthConnectPreferencesSnapshot(
    val userOptIn: Boolean = false,
    val permissionsPreviouslyGranted: Boolean = false,
)

@Singleton
class HealthConnectPreferencesRepository
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val dataStore = context.healthConnectDataStore

        val snapshot: Flow<HealthConnectPreferencesSnapshot> =
            dataStore.data.map { prefs ->
                HealthConnectPreferencesSnapshot(
                    userOptIn = prefs[HealthConnectPreferenceKeys.userOptIn] ?: false,
                    permissionsPreviouslyGranted =
                        prefs[HealthConnectPreferenceKeys.permissionsPreviouslyGranted] ?: false,
                )
            }

        suspend fun setUserOptIn(enabled: Boolean) {
            dataStore.edit { prefs ->
                prefs[HealthConnectPreferenceKeys.userOptIn] = enabled
            }
        }

        suspend fun setPermissionsPreviouslyGranted(granted: Boolean) {
            dataStore.edit { prefs ->
                prefs[HealthConnectPreferenceKeys.permissionsPreviouslyGranted] = granted
            }
        }

        suspend fun clearIntegrationState() {
            dataStore.edit { prefs ->
                prefs[HealthConnectPreferenceKeys.userOptIn] = false
                prefs[HealthConnectPreferenceKeys.permissionsPreviouslyGranted] = false
            }
        }
    }
