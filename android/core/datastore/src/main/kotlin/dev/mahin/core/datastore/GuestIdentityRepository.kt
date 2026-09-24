package dev.mahin.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.mahin.core.model.AnalyticsInstallationId
import dev.mahin.core.model.GuestIdentity
import dev.mahin.core.model.LocalUserId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.guestDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mahin_guest_identity",
)

@Singleton
class GuestIdentityRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.guestDataStore

        suspend fun ensureGuestIdentity(): GuestIdentity {
            val prefs = dataStore.data.first()
            val existing = prefs[OnboardingPreferenceKeys.localUserId]
            if (existing != null) {
                return GuestIdentity(
                    localUserId = LocalUserId(UUID.fromString(existing)),
                    createdAtEpochMs = System.currentTimeMillis(),
                )
            }
            val newId = UUID.randomUUID()
            val analyticsId = UUID.randomUUID()
            dataStore.edit { store ->
                store[OnboardingPreferenceKeys.localUserId] = newId.toString()
                store[OnboardingPreferenceKeys.analyticsInstallationId] = analyticsId.toString()
            }
            return GuestIdentity(
                localUserId = LocalUserId(newId),
                createdAtEpochMs = System.currentTimeMillis(),
            )
        }

        suspend fun analyticsInstallationId(): AnalyticsInstallationId? =
            dataStore.data
                .map { prefs ->
                    prefs[OnboardingPreferenceKeys.analyticsInstallationId]?.let {
                        AnalyticsInstallationId(UUID.fromString(it))
                    }
                }.first()
    }
