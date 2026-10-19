package dev.mahin.android.cycle.editor

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.PainLocation
import dev.mahin.core.testing.MainDispatcherRule
import java.time.LocalDate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DayEditorOpenDateMutationTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule =
        MainDispatcherRule(
            testDispatcher,
            beforeResetMain = {
                testDispatcher.scheduler.advanceUntilIdle()
                DayEditorTestVmRegistry.assertAllStoresClosed()
                org.robolectric.shadows.ShadowLooper
                    .idleMainLooper()
            },
        )

    private val day = LocalDate.of(2025, 8, 15)

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @get:Rule
    val testName = TestName()

    @Test
    fun setPain_immediatelyAfterOpenDate_doesNotNoOp() =
        runTest(testDispatcher) {
            val harness = dayEditorHarness(day, javaClass, testName, temporaryFolder, testDispatcher)
            try {
                harness.seedCycleProfile()
                val viewModel = harness.createViewModel()
                viewModel.openDate(day)
                viewModel.setPain(PainLocation.HEAD, 4)
                viewModel.uiState.filter { it.painSeverity == 4 }.first()
                assertThat(harness.dailyLogFor(day)?.painSeverity).isEqualTo(4)
            } finally {
                testDispatcher.scheduler.advanceUntilIdle()
                org.robolectric.shadows.ShadowLooper
                    .idleMainLooper()
                harness.close()
            }
        }
}
