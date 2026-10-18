package dev.mahin.android.cycle.editor

import dev.mahin.core.model.LogEditorCategory
import dev.mahin.core.testing.MainDispatcherRule
import java.time.LocalDate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DayEditorLifestylePersistenceTest {
    private val testDispatcher = StandardTestDispatcher()
    private val day = LocalDate.of(2025, 8, 2)

    @get:Rule
    val mainDispatcherRule =
        MainDispatcherRule(
            testDispatcher,
            beforeResetMain = {
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
            },
        )

    @Test
    fun toggleSleep_persistsToDailyLog() =
        runTest(testDispatcher) {
            val harness = DayEditorTestHarness.create(day)
            try {
                harness.seedCycleProfile()
                val viewModel = harness.createViewModel()
                harness.loggingPreferencesRepository.setLifestyleModuleEnabled(LogEditorCategory.SLEEP, true)
                viewModel.openDateAndAwait(day)
                viewModel.toggleLifestyleLogged(LogEditorCategory.SLEEP)
                viewModel.uiState.filter { it.sleepLogged }.first()
                viewModel.toggleLifestyleLogged(LogEditorCategory.SLEEP)
                viewModel.uiState.filter { !it.sleepLogged }.first()
            } finally {
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
                harness.close()
            }
        }
}
