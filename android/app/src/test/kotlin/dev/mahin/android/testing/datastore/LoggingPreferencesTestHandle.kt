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
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import org.junit.rules.TemporaryFolder

/**
 * Per-test [LoggingPreferencesRepository] backed by an isolated on-disk DataStore (not
 * [androidx.datastore.preferences.preferencesDataStore] on [android.content.Context]).
 *
 * Cancel [scope] only from [closeAndCancelScope] after [androidx.lifecycle.ViewModelStore.clear]
 * and draining [Dispatchers.Main] (see Day Editor harness [drainCoroutineWork]); the factory
 * [scope] uses [Dispatchers.IO] so DataStore I/O does not deadlock [kotlinx.coroutines.test.runTest].
 */
class LoggingPreferencesTestHandle private constructor(
    val repository: LoggingPreferencesRepository,
    private val scope: CoroutineScope,
    private val dataStoreRoot: File,
) : AutoCloseable {
    override fun close() {
        // DayEditor harnesses call this after [ViewModelStoreTestHarness.clear()]; do not cancel
        // [scope] here — see [closeAndCancelScope] for tests that fully release ViewModels first.
    }

    /** Call only after every ViewModel using [repository] has been cleared. */
    fun closeAndCancelScope(drain: () -> Unit = {}) {
        scope.cancel()
        var spins = 0
        while (scope.coroutineContext.job.isActive && spins < 256) {
            drain()
            spins++
        }
        runBlocking {
            scope.coroutineContext.job.join()
        }
    }

    companion object {
        fun open(
            temporaryFolder: TemporaryFolder,
            scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        ): LoggingPreferencesTestHandle = openInDirectory(temporaryFolder.newFolder("logging-prefs"), scope)

        fun openInDirectory(
            root: File,
            scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        ): LoggingPreferencesTestHandle {
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
