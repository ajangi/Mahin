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

private val Context.appLockDataStore: DataStore<Preferences> by preferencesDataStore(name = "mahin_app_lock_prefs")

data class AppLockPreferencesSnapshot(
    val mode: AppLockMode = AppLockMode.DISABLED,
    val hideRecentsWhenLocked: Boolean = true,
    val blockScreenshotsOnSensitiveScreens: Boolean = true,
)

@Singleton
class AppLockPreferencesRepository
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val dataStore = context.appLockDataStore

        val snapshot: Flow<AppLockPreferencesSnapshot> =
            dataStore.data.map { prefs ->
                val modeRaw = prefs[PrivacyPreferenceKeys.appLockMode]
                AppLockPreferencesSnapshot(
                    mode = modeRaw?.let { runCatching { AppLockMode.valueOf(it) }.getOrNull() } ?: AppLockMode.DISABLED,
                    hideRecentsWhenLocked = prefs[PrivacyPreferenceKeys.hideRecentsWhenLocked] ?: true,
                    blockScreenshotsOnSensitiveScreens =
                        prefs[PrivacyPreferenceKeys.blockScreenshotsOnSensitiveScreens] ?: true,
                )
            }

        suspend fun setMode(mode: AppLockMode) {
            dataStore.edit { prefs ->
                prefs[PrivacyPreferenceKeys.appLockMode] = mode.name
            }
        }

        suspend fun setHideRecentsWhenLocked(enabled: Boolean) {
            dataStore.edit { prefs ->
                prefs[PrivacyPreferenceKeys.hideRecentsWhenLocked] = enabled
            }
        }

        suspend fun setBlockScreenshotsOnSensitiveScreens(enabled: Boolean) {
            dataStore.edit { prefs ->
                prefs[PrivacyPreferenceKeys.blockScreenshotsOnSensitiveScreens] = enabled
            }
        }
    }
