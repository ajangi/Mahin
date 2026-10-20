package dev.mahin.android.cycle.editor

import dev.mahin.android.testing.datastore.LoggingPreferencesTestHandle
import java.time.LocalDate
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestName
import org.robolectric.shadows.ShadowLooper

internal fun dayEditorHarness(
    today: LocalDate,
    testClass: Class<*>,
    testName: TestName,
    temporaryFolder: TemporaryFolder,
    testDispatcher: TestDispatcher,
): DayEditorTestHarness {
    val dataStoreDispatcher = UnconfinedTestDispatcher()
    val drainCoroutineWork = {
        testDispatcher.scheduler.advanceUntilIdle()
        dataStoreDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        ShadowLooper.idleMainLooper()
    }
    val loggingPreferencesHandle = LoggingPreferencesTestHandle.open(temporaryFolder, dataStoreDispatcher)
    return DayEditorTestHarness.create(
        today = today,
        registryOwner = "${testClass.simpleName}.${testName.methodName}",
        loggingPreferencesHandle = loggingPreferencesHandle,
        drainCoroutineWork = drainCoroutineWork,
        testDispatcher = dataStoreDispatcher,
    )
}

/** Compose / Robolectric tests without a [TestDispatcher] on the main looper. */
internal fun dayEditorHarnessOnMain(
    today: LocalDate,
    testClass: Class<*>,
    testName: TestName,
    temporaryFolder: TemporaryFolder,
): DayEditorTestHarness {
    val dataStoreDispatcher = UnconfinedTestDispatcher()
    val drainCoroutineWork = {
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        ShadowLooper.idleMainLooper()
    }
    val loggingPreferencesHandle = LoggingPreferencesTestHandle.open(temporaryFolder, dataStoreDispatcher)
    return DayEditorTestHarness.create(
        today = today,
        registryOwner = "${testClass.simpleName}.${testName.methodName}",
        loggingPreferencesHandle = loggingPreferencesHandle,
        drainCoroutineWork = drainCoroutineWork,
        testDispatcher = dataStoreDispatcher,
    )
}
