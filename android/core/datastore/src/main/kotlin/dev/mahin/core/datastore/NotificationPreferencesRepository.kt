package dev.mahin.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.mahin.domain.reminders.ReminderCategory
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mahin_notification_preferences",
)

data class NotificationPreferenceSnapshot(
    val privacyMode: NotificationPrivacyMode,
    val zoneId: String,
    val localHour: Int,
    val localMinute: Int,
    val categoryEnabled: Map<ReminderCategory, Boolean>,
)

@Singleton
class NotificationPreferencesRepository
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val dataStore = context.notificationPreferencesDataStore

        fun observePrivacyMode(): Flow<NotificationPrivacyMode> =
            dataStore.data.map { prefs ->
                prefs[PrivacyPreferenceKeys.notificationPrivacyMode]?.let { raw ->
                    runCatching { NotificationPrivacyMode.valueOf(raw) }.getOrNull()
                } ?: NotificationPrivacyMode.DISCREET
            }

        suspend fun setPrivacyMode(mode: NotificationPrivacyMode) {
            dataStore.edit { it[PrivacyPreferenceKeys.notificationPrivacyMode] = mode.name }
        }

        fun observeZoneId(): Flow<String> =
            dataStore.data.map { prefs ->
                prefs[ReminderPreferenceKeys.reminderZoneId] ?: ZoneId.systemDefault().id
            }

        suspend fun setZoneId(zoneId: String) {
            dataStore.edit { it[ReminderPreferenceKeys.reminderZoneId] = zoneId }
        }

        fun observeLocalTime(): Flow<Pair<Int, Int>> =
            dataStore.data.map { prefs ->
                val hour = prefs[ReminderPreferenceKeys.reminderLocalHour] ?: 9
                val minute = prefs[ReminderPreferenceKeys.reminderLocalMinute] ?: 0
                hour to minute
            }

        suspend fun setLocalTime(
            hour: Int,
            minute: Int,
        ) {
            dataStore.edit {
                it[ReminderPreferenceKeys.reminderLocalHour] = hour.coerceIn(0, 23)
                it[ReminderPreferenceKeys.reminderLocalMinute] = minute.coerceIn(0, 59)
            }
        }

        fun observeCategoryEnabled(category: ReminderCategory): Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[ReminderPreferenceKeys.categoryEnabled(category)] ?: false
            }

        suspend fun setCategoryEnabled(
            category: ReminderCategory,
            enabled: Boolean,
        ) {
            dataStore.edit { it[ReminderPreferenceKeys.categoryEnabled(category)] = enabled }
        }

        fun observeSnapshot(): Flow<NotificationPreferenceSnapshot> =
            dataStore.data.map { prefs ->
                val mode =
                    prefs[PrivacyPreferenceKeys.notificationPrivacyMode]?.let { raw ->
                        runCatching { NotificationPrivacyMode.valueOf(raw) }.getOrNull()
                    } ?: NotificationPrivacyMode.DISCREET
                val zone = prefs[ReminderPreferenceKeys.reminderZoneId] ?: ZoneId.systemDefault().id
                val hour = prefs[ReminderPreferenceKeys.reminderLocalHour] ?: 9
                val minute = prefs[ReminderPreferenceKeys.reminderLocalMinute] ?: 0
                val categories =
                    ReminderCategory.entries.associateWith { category ->
                        prefs[ReminderPreferenceKeys.categoryEnabled(category)] ?: false
                    }
                NotificationPreferenceSnapshot(
                    privacyMode = mode,
                    zoneId = zone,
                    localHour = hour,
                    localMinute = minute,
                    categoryEnabled = categories,
                )
            }
    }
