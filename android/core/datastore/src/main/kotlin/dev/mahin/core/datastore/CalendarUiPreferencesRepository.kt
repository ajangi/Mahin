package dev.mahin.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.calendarUiDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mahin_calendar_ui",
)

private object CalendarUiPreferenceKeys {
    val legendCollapsed = booleanPreferencesKey("legend_collapsed")
    val legendAutoCollapsedOnce = booleanPreferencesKey("legend_auto_collapsed_once")
}

@Singleton
class CalendarUiPreferencesRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.calendarUiDataStore

        fun observeLegendCollapsed(): Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[CalendarUiPreferenceKeys.legendCollapsed] == true
            }

        fun observeLegendAutoCollapsedOnce(): Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[CalendarUiPreferenceKeys.legendAutoCollapsedOnce] == true
            }

        suspend fun setLegendCollapsed(collapsed: Boolean) {
            dataStore.edit { prefs ->
                prefs[CalendarUiPreferenceKeys.legendCollapsed] = collapsed
            }
        }

        suspend fun markLegendAutoCollapsedOnce() {
            dataStore.edit { prefs ->
                prefs[CalendarUiPreferenceKeys.legendAutoCollapsedOnce] = true
                prefs[CalendarUiPreferenceKeys.legendCollapsed] = true
            }
        }

        suspend fun clear() {
            dataStore.edit { it.clear() }
        }
    }
