package dev.mahin.android.cycle.editor

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.PeriodFlowLevel
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DayEditorFlowLevelTest {
    private lateinit var harness: DayEditorTestHarness
    private lateinit var viewModel: DayEditorViewModel
    private val day = LocalDate.of(2025, 7, 1)

    @Before
    fun setUp() {
        harness = DayEditorTestHarness.create(day)
        runBlocking { harness.seedCycleProfile() }
        viewModel = harness.createViewModel()
        runBlocking {
            viewModel.openDate(day)
            viewModel.togglePeriodLogged()
            awaitUntil { viewModel.uiState.value.periodLogged }
        }
    }

    @After
    fun tearDown() {
        harness.close()
    }

    @Test
    fun setFlowLevel_persistsToPeriodDay() {
        runBlocking {
            viewModel.setFlowLevel(PeriodFlowLevel.HEAVY)
            awaitUntil {
                harness.periodDayFor(day)?.flowLevel == PeriodFlowLevel.HEAVY
            }
            assertThat(viewModel.uiState.value.flowLevel).isEqualTo(PeriodFlowLevel.HEAVY)
        }
    }

    private suspend fun awaitUntil(
        timeoutMs: Long = 5_000,
        condition: () -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            ShadowLooper.idleMainLooper()
        }
        throw AssertionError("Condition not met within ${timeoutMs}ms")
    }
}
