package dev.mahin.android.cycle.editor

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.PeriodFlowLevel
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
class DayEditorFlowLevelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val day = LocalDate.of(2025, 7, 1)

    @get:Rule
    val mainDispatcherRule =
        MainDispatcherRule(
            testDispatcher,
            beforeResetMain = {
                testDispatcher.scheduler.advanceUntilIdle()
                DayEditorTestVmRegistry.assertAllStoresClosed()
                ShadowLooper.idleMainLooper()
            },
        )

    @Test
    fun setFlowLevel_persistsToPeriodDay() =
        runTest(testDispatcher) {
            val harness = DayEditorTestHarness.create(day)
            try {
                harness.seedCycleProfile()
                val viewModel = harness.createViewModel()
                viewModel.openDateAndAwait(day)
                viewModel.togglePeriodLogged()
                viewModel.uiState.filter { it.periodLogged }.first()
                viewModel.setFlowLevel(PeriodFlowLevel.HEAVY)
                viewModel.uiState.filter { it.flowLevel == PeriodFlowLevel.HEAVY }.first()
                harness.awaitPeriodDayFlowLevel(day, PeriodFlowLevel.HEAVY)
                assertThat(viewModel.uiState.value.flowLevel).isEqualTo(PeriodFlowLevel.HEAVY)
                assertThat(harness.periodDayFor(day)?.flowLevel).isEqualTo(PeriodFlowLevel.HEAVY)
            } finally {
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
                harness.close()
            }
        }
}
