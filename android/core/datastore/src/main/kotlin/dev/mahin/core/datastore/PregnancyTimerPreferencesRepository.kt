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

private val Context.pregnancyTimerDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mahin_pregnancy_timers",
)

data class KickTimerSnapshot(
    val sessionId: String?,
    val startedAtEpochMs: Long?,
)

data class ContractionTimerSnapshot(
    val sessionId: String?,
    val openEventId: String?,
    val openEventStartedEpochMs: Long?,
)

@Singleton
class PregnancyTimerPreferencesRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.pregnancyTimerDataStore

        fun observeKickTimer(): Flow<KickTimerSnapshot> =
            dataStore.data.map { prefs ->
                KickTimerSnapshot(
                    sessionId = prefs[PregnancyTimerPreferenceKeys.activeKickSessionId],
                    startedAtEpochMs = prefs[PregnancyTimerPreferenceKeys.activeKickStartedEpochMs],
                )
            }

        fun observeContractionTimer(): Flow<ContractionTimerSnapshot> =
            dataStore.data.map { prefs ->
                ContractionTimerSnapshot(
                    sessionId = prefs[PregnancyTimerPreferenceKeys.activeContractionSessionId],
                    openEventId = prefs[PregnancyTimerPreferenceKeys.activeContractionEventId],
                    openEventStartedEpochMs =
                        prefs[PregnancyTimerPreferenceKeys.activeContractionEventStartedEpochMs],
                )
            }

        suspend fun setActiveKickSession(
            sessionId: String?,
            startedAtEpochMs: Long?,
        ) {
            dataStore.edit { prefs ->
                if (sessionId == null) {
                    prefs.remove(PregnancyTimerPreferenceKeys.activeKickSessionId)
                    prefs.remove(PregnancyTimerPreferenceKeys.activeKickStartedEpochMs)
                } else {
                    prefs[PregnancyTimerPreferenceKeys.activeKickSessionId] = sessionId
                    prefs[PregnancyTimerPreferenceKeys.activeKickStartedEpochMs] = startedAtEpochMs ?: 0L
                }
            }
        }

        suspend fun setActiveContractionTimer(
            sessionId: String?,
            openEventId: String?,
            openEventStartedEpochMs: Long?,
        ) {
            dataStore.edit { prefs ->
                if (sessionId == null) {
                    prefs.remove(PregnancyTimerPreferenceKeys.activeContractionSessionId)
                    prefs.remove(PregnancyTimerPreferenceKeys.activeContractionEventId)
                    prefs.remove(PregnancyTimerPreferenceKeys.activeContractionEventStartedEpochMs)
                } else {
                    prefs[PregnancyTimerPreferenceKeys.activeContractionSessionId] = sessionId
                    if (openEventId == null) {
                        prefs.remove(PregnancyTimerPreferenceKeys.activeContractionEventId)
                        prefs.remove(PregnancyTimerPreferenceKeys.activeContractionEventStartedEpochMs)
                    } else {
                        prefs[PregnancyTimerPreferenceKeys.activeContractionEventId] = openEventId
                        prefs[PregnancyTimerPreferenceKeys.activeContractionEventStartedEpochMs] =
                            openEventStartedEpochMs ?: 0L
                    }
                }
            }
        }
    }
