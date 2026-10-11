package dev.mahin.android.cycle.editor

import dev.mahin.core.model.LogEditorCategory
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
class DayEditorLifestylePersistenceTest {
    private lateinit var harness: DayEditorTestHarness
    private lateinit var viewModel: DayEditorViewModel
    private val day = LocalDate.of(2025, 8, 2)

    @Before
    fun setUp() {
        harness = DayEditorTestHarness.create(day)
        runBlocking { harness.seedCycleProfile() }
        viewModel = harness.createViewModel()
        runBlocking {
            harness.loggingPreferencesRepository.setLifestyleModuleEnabled(LogEditorCategory.SLEEP, true)
            viewModel.openDate(day)
            awaitUntil { viewModel.uiState.value.date == day }
        }
    }

    @After
    fun tearDown() {
        harness.close()
    }

    @Test
    fun toggleSleep_persistsToDailyLog() {
        runBlocking {
            viewModel.toggleLifestyleLogged(LogEditorCategory.SLEEP)
            awaitUntil { harness.dailyLogFor(day)?.sleepLogged == true }
            viewModel.toggleLifestyleLogged(LogEditorCategory.SLEEP)
            awaitUntil {
                val log = harness.dailyLogFor(day)
                log != null && !log.sleepLogged
            }
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
