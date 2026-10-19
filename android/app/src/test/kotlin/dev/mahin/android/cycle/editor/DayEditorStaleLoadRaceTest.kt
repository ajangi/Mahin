package dev.mahin.android.cycle.editor

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.PainLocation
import dev.mahin.core.testing.MainDispatcherRule
import java.time.LocalDate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestName
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DayEditorStaleLoadRaceTest {
    private val testDispatcher = StandardTestDispatcher()
    private val day = LocalDate.of(2025, 8, 20)

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @get:Rule
    val testName = TestName()

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

    private var harness: DayEditorTestHarness? = null

    @After
    fun tearDown() {
        harness?.close()
        harness = null
    }

    @Test
    fun staleOpenDateLoad_doesNotOverwriteLaterMutation() =
        runTest(testDispatcher) {
            harness = dayEditorHarness(day, javaClass, testName, temporaryFolder, testDispatcher)
            harness!!.seedCycleProfile()
            val viewModel = harness!!.createViewModel()
            var loadsBeforeApply = 0
            viewModel.beforeApplyDayStateHook = {
                loadsBeforeApply++
                if (loadsBeforeApply == 1) {
                    viewModel.setPain(PainLocation.HEAD, 6)
                    advanceUntilIdle()
                    ShadowLooper.idleMainLooper()
                }
            }
            viewModel.openDate(day)
            viewModel.uiState.filter { it.painSeverity == 6 }.first()
            advanceUntilIdle()
            ShadowLooper.idleMainLooper()
            assertThat(viewModel.uiState.value.painSeverity).isEqualTo(6)
            assertThat(harness!!.dailyLogFor(day)?.painSeverity).isEqualTo(6)
            viewModel.beforeApplyDayStateHook = null
        }
}
