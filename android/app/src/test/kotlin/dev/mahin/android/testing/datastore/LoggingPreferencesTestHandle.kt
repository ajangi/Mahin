package dev.mahin.android.testing.datastore

import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import dev.mahin.core.datastore.LoggingPreferencesRepository
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import org.junit.rules.TemporaryFolder

/**
 * Per-test [LoggingPreferencesRepository] backed by an isolated on-disk DataStore (not
 * [androidx.datastore.preferences.preferencesDataStore] on [android.content.Context]).
 *
 * The factory [scope] uses [testDispatcher] so reads/writes are drained with
 * [TestDispatcher.scheduler.advanceUntilIdle] before the JUnit [TemporaryFolder] is deleted.
 * Cancel [scope] only from [closeAndCancelScope] after [androidx.lifecycle.ViewModelStore.clear]
 * and draining the main/test schedulers.
 */
class LoggingPreferencesTestHandle private constructor(
    val repository: LoggingPreferencesRepository,
    private val scope: CoroutineScope,
    private val testDispatcher: TestDispatcher,
    private val dataStoreRoot: File,
) : AutoCloseable {
    override fun close() {
        // DayEditor harnesses call this after [ViewModelStoreTestHarness.clear()]; do not cancel
        // [scope] here — see [closeAndCancelScope] for tests that fully release ViewModels first.
    }

    /** Call only after every ViewModel using [repository] has been cleared. */
    fun closeAndCancelScope() {
        scope.cancel()
        runBlocking(testDispatcher) {
            testDispatcher.scheduler.advanceUntilIdle()
            scope.coroutineContext.job.join()
        }
    }

    companion object {
        fun open(
            temporaryFolder: TemporaryFolder,
            testDispatcher: TestDispatcher,
        ): LoggingPreferencesTestHandle =
            openInDirectory(
                temporaryFolder.newFolder("logging-prefs-${System.nanoTime()}"),
                testDispatcher,
            )

        fun openInDirectory(
            root: File,
            testDispatcher: TestDispatcher,
        ): LoggingPreferencesTestHandle {
            val scope = CoroutineScope(SupervisorJob() + testDispatcher)
            val dataStore =
                PreferenceDataStoreFactory.create(
                    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
                    scope = scope,
                    produceFile = { File(root, "mahin_logging_preferences.preferences_pb") },
                )
            return LoggingPreferencesTestHandle(
                repository = LoggingPreferencesRepository.createForTests(dataStore),
                scope = scope,
                testDispatcher = testDispatcher,
                dataStoreRoot = root,
            )
        }
    }
}
