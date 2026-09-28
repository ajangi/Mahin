package dev.mahin.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PrivacyPreferenceKeys {
    val notificationsEnabled = booleanPreferencesKey("notifications_enabled")
    val notificationPrivacyMode = stringPreferencesKey("notification_privacy_mode")
    val analyticsEnabled = booleanPreferencesKey("analytics_enabled")
    val appLockMode = stringPreferencesKey("app_lock_mode")
    val hideRecentsWhenLocked = booleanPreferencesKey("hide_recents_when_locked")
    val blockScreenshotsOnSensitiveScreens = booleanPreferencesKey("block_screenshots_sensitive")
}

enum class NotificationPrivacyMode {
    DISCREET,
    DESCRIPTIVE,
    OFF,
}

enum class AppLockMode {
    DISABLED,
    PIN,
    BIOMETRIC,
}
