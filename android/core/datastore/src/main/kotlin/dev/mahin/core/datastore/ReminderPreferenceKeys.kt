package dev.mahin.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.mahin.domain.reminders.ReminderCategory

object ReminderPreferenceKeys {
    val reminderZoneId = stringPreferencesKey("reminder_zone_id")
    val reminderLocalHour = intPreferencesKey("reminder_local_hour")
    val reminderLocalMinute = intPreferencesKey("reminder_local_minute")

    fun categoryEnabled(category: ReminderCategory) = booleanPreferencesKey("reminder_cat_${category.name.lowercase()}")
}
