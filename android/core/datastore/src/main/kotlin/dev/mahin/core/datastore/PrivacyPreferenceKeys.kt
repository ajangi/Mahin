package dev.mahin.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PrivacyPreferenceKeys {
    val notificationsEnabled = booleanPreferencesKey("notifications_enabled")
    val notificationPrivacyMode = stringPreferencesKey("notification_privacy_mode")
    val analyticsEnabled = booleanPreferencesKey("analytics_enabled")
}

enum class NotificationPrivacyMode {
    DISCREET,
    DESCRIPTIVE,
    OFF,
}
