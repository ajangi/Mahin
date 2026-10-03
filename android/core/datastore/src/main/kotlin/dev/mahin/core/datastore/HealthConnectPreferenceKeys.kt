package dev.mahin.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey

object HealthConnectPreferenceKeys {
    val userOptIn = booleanPreferencesKey("health_connect_user_opt_in")
    val permissionsPreviouslyGranted = booleanPreferencesKey("health_connect_permissions_granted")
}
