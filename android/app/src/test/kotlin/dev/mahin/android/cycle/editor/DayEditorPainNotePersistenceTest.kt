package dev.mahin.android.cycle.editor

import dev.mahin.core.model.PainLocation
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
class DayEditorPainNotePersistenceTest {
    private lateinit var harness: DayEditorTestHarness
    private lateinit var viewModel: DayEditorViewModel
    private val day = LocalDate.of(2025, 9, 9)

    @Before
    fun setUp() {
        harness = DayEditorTestHarness.create(day)
        runBlocking { harness.seedCycleProfile() }
        viewModel = harness.createViewModel()
        runBlocking {
            viewModel.openDate(day)
            viewModel.setPain(PainLocation.HEAD, 5)
            awaitUntil { viewModel.uiState.value.painSeverity == 5 }
        }
    }

    @After
    fun tearDown() {
        harness.close()
    }

    @Test
    fun setPainNote_persistsToDailyLog() {
        runBlocking {
            viewModel.setPainNote("درد شدید")
            awaitUntil { harness.dailyLogFor(day)?.painNote == "درد شدید" }
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
