package dev.mahin.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

private val Context.periodDayTombstoneStore: DataStore<Preferences> by preferencesDataStore(
    name = "health_connect_period_day_tombstones",
)

/**
 * Local tombstones for user-deleted period days (see docs/health-connect/DELETED_PERIOD_DAY_POLICY.md).
 */
@Singleton
class HealthConnectPeriodDayTombstoneRepository
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val dataStore = context.periodDayTombstoneStore
        private val key = stringSetPreferencesKey("deleted_iso_dates")

        suspend fun isUserDeleted(date: LocalDate): Boolean = userDeletedDatesInRange(date, date).contains(date)

        suspend fun userDeletedDatesInRange(
            start: LocalDate,
            end: LocalDate,
        ): Set<LocalDate> {
            val stored = dataStore.data.first()[key].orEmpty()
            return stored
                .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
                .filter { !it.isBefore(start) && !it.isAfter(end) }
                .toSet()
        }

        suspend fun markUserDeleted(date: LocalDate) {
            dataStore.edit { prefs ->
                val updated = prefs[key].orEmpty().toMutableSet()
                updated.add(date.toString())
                prefs[key] = updated
            }
        }

        suspend fun clearUserDeleted(date: LocalDate) {
            dataStore.edit { prefs ->
                val updated = prefs[key].orEmpty().toMutableSet()
                updated.remove(date.toString())
                prefs[key] = updated
            }
        }

        suspend fun clearAll() {
            dataStore.edit { it.remove(key) }
        }
    }
