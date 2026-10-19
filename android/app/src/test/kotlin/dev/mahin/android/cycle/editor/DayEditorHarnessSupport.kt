package dev.mahin.android.cycle.editor

import dev.mahin.android.testing.datastore.LoggingPreferencesTestHandle
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
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
    val drainCoroutineWork = {
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        ShadowLooper.idleMainLooper()
    }
    val loggingPreferencesHandle = LoggingPreferencesTestHandle.open(temporaryFolder)
    return DayEditorTestHarness.create(
        today = today,
        registryOwner = "${testClass.simpleName}.${testName.methodName}",
        loggingPreferencesHandle = loggingPreferencesHandle,
        drainCoroutineWork = drainCoroutineWork,
    )
}

/** Compose / Robolectric tests without a [TestDispatcher] on [Dispatchers.Main]. */
internal fun dayEditorHarnessOnMain(
    today: LocalDate,
    testClass: Class<*>,
    testName: TestName,
    temporaryFolder: TemporaryFolder,
): DayEditorTestHarness {
    val drainCoroutineWork = {
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        ShadowLooper.idleMainLooper()
    }
    val loggingPreferencesHandle = LoggingPreferencesTestHandle.open(temporaryFolder)
    return DayEditorTestHarness.create(
        today = today,
        registryOwner = "${testClass.simpleName}.${testName.methodName}",
        loggingPreferencesHandle = loggingPreferencesHandle,
        drainCoroutineWork = drainCoroutineWork,
    )
}
