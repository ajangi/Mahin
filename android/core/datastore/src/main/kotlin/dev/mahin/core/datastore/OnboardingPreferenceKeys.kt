package dev.mahin.core.datastore

import androidx.datastore.preferences.core.stringPreferencesKey

object OnboardingPreferenceKeys {
    val localUserId = stringPreferencesKey("local_user_id")
    val analyticsInstallationId = stringPreferencesKey("analytics_installation_id")
}
