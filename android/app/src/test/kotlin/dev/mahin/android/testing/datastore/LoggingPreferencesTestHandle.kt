package dev.mahin.android.testing.datastore

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import dev.mahin.core.datastore.LoggingPreferencesRepository
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.rules.TemporaryFolder

/**
 * Per-test [LoggingPreferencesRepository] backed by an isolated on-disk DataStore (not
 * [androidx.datastore.preferences.preferencesDataStore] on [android.content.Context]).
 */
class LoggingPreferencesTestHandle private constructor(
    val repository: LoggingPreferencesRepository,
    private val scope: CoroutineScope,
    private val dataStoreRoot: File,
) : AutoCloseable {
    override fun close() {
        scope.cancel()
        dataStoreRoot.deleteRecursively()
    }

    companion object {
        fun open(temporaryFolder: TemporaryFolder): LoggingPreferencesTestHandle =
            openInDirectory(temporaryFolder.newFolder("logging-prefs"))

        fun openInDirectory(root: File): LoggingPreferencesTestHandle {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val dataStore =
                PreferenceDataStoreFactory.create(
                    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                    scope = scope,
                    produceFile = { File(root, "mahin_logging_preferences.preferences_pb") },
                )
            return LoggingPreferencesTestHandle(
                repository = LoggingPreferencesRepository.createForTests(dataStore),
                scope = scope,
                dataStoreRoot = root,
            )
        }
    }
}
