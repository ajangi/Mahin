package dev.mahin.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.mahin.core.model.LogEditorCategory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.loggingPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mahin_logging_preferences",
)

@Singleton
class LoggingPreferencesRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val dataStore = context.loggingPreferencesDataStore

        fun observeSexualActivityEnabled(): Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[LoggingPreferenceKeys.sexualActivityEnabled] ?: false
            }

        suspend fun setSexualActivityEnabled(enabled: Boolean) {
            dataStore.edit { it[LoggingPreferenceKeys.sexualActivityEnabled] = enabled }
        }

        fun observeLifestyleModuleEnabled(category: LogEditorCategory): Flow<Boolean> =
            dataStore.data.map { prefs ->
                when (category) {
                    LogEditorCategory.SLEEP,
                    LogEditorCategory.EXERCISE,
                    LogEditorCategory.WATER,
                    LogEditorCategory.STRESS,
                    ->
                        prefs[LoggingPreferenceKeys.lifestyleEnabled(category)] ?: false
                    else -> false
                }
            }

        suspend fun setLifestyleModuleEnabled(
            category: LogEditorCategory,
            enabled: Boolean,
        ) {
            dataStore.edit { it[LoggingPreferenceKeys.lifestyleEnabled(category)] = enabled }
        }

        fun observeMostUsedFirst(): Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[LoggingPreferenceKeys.mostUsedFirst] ?: false
            }

        suspend fun setMostUsedFirst(enabled: Boolean) {
            dataStore.edit { it[LoggingPreferenceKeys.mostUsedFirst] = enabled }
        }

        fun observePinnedCategoryOrder(): Flow<List<LogEditorCategory>> =
            dataStore.data.map { prefs ->
                val raw = prefs[LoggingPreferenceKeys.pinnedOrder] ?: return@map emptyList()
                raw.split(',').mapNotNull { token ->
                    runCatching { LogEditorCategory.valueOf(token) }.getOrNull()
                }
            }

        suspend fun setPinnedCategoryOrder(order: List<LogEditorCategory>) {
            dataStore.edit {
                it[LoggingPreferenceKeys.pinnedOrder] = order.joinToString(",") { cat -> cat.name }
            }
        }

        suspend fun clear() {
            dataStore.edit { it.clear() }
        }
    }

private object LoggingPreferenceKeys {
    val sexualActivityEnabled = booleanPreferencesKey("sexual_activity_enabled")
    val mostUsedFirst = booleanPreferencesKey("most_used_first")
    val pinnedOrder = stringPreferencesKey("pinned_category_order")

    fun lifestyleEnabled(category: LogEditorCategory) =
        booleanPreferencesKey("lifestyle_${category.name.lowercase()}")
}
