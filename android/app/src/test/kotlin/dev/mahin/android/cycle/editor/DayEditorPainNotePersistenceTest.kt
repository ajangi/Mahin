package dev.mahin.android.cycle.editor

import dev.mahin.core.model.PainLocation
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
class DayEditorPainNotePersistenceTest {
    private val testDispatcher = StandardTestDispatcher()
    private val day = LocalDate.of(2025, 9, 9)

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
    fun setPainNote_persistsToDailyLog() =
        runTest(testDispatcher) {
            val harness = DayEditorTestHarness.create(day)
            try {
                harness.seedCycleProfile()
                val viewModel = harness.createViewModel()
                viewModel.openDateAndAwait(day)
                viewModel.setPain(PainLocation.HEAD, 5)
                viewModel.uiState.filter { it.painSeverity == 5 }.first()
                viewModel.setPainNote("درد شدید")
                viewModel.uiState.filter { it.painNote == "درد شدید" }.first()
            } finally {
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
                harness.close()
            }
        }
}
