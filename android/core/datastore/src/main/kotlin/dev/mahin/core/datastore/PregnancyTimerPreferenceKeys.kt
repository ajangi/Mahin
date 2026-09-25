package dev.mahin.core.datastore

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PregnancyTimerPreferenceKeys {
    val activeKickSessionId = stringPreferencesKey("active_kick_session_id")
    val activeKickStartedEpochMs = longPreferencesKey("active_kick_started_epoch_ms")
    val activeContractionSessionId = stringPreferencesKey("active_contraction_session_id")
    val activeContractionEventId = stringPreferencesKey("active_contraction_event_id")
    val activeContractionEventStartedEpochMs = longPreferencesKey("active_contraction_event_started_epoch_ms")
}
