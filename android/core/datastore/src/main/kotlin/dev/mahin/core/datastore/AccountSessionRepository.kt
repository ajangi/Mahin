package dev.mahin.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.accountSessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mahin_account_session",
)

@Singleton
class AccountSessionRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.accountSessionDataStore

        val accessToken: Flow<String?> =
            dataStore.data.map { prefs -> prefs[AccountSessionKeys.accessToken] }

        suspend fun setAccessToken(token: String?) {
            dataStore.edit { prefs ->
                if (token == null) {
                    prefs.remove(AccountSessionKeys.accessToken)
                } else {
                    prefs[AccountSessionKeys.accessToken] = token
                }
            }
        }
    }

private object AccountSessionKeys {
    val accessToken = stringPreferencesKey("access_token")
}
