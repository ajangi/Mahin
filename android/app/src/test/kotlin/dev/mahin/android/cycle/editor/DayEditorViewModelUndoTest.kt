package dev.mahin.android.cycle.editor

import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.LogEditorCategory
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PregnancyTestResult
import dev.mahin.core.model.ReproductiveMode
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
class DayEditorViewModelUndoTest {
    private lateinit var harness: DayEditorTestHarness
    private lateinit var viewModel: DayEditorViewModel
    private val day = LocalDate.of(2025, 6, 15)

    @Before
    fun setUp() {
        harness = DayEditorTestHarness.create(day)
        runBlocking { harness.seedCycleProfile(ReproductiveMode.TRYING_TO_CONCEIVE) }
        viewModel = harness.createViewModel()
        runBlocking {
            harness.loggingPreferencesRepository.setSexualActivityEnabled(true)
            viewModel.openDate(day)
            awaitUntil { viewModel.uiState.value.date == day }
        }
    }

    @After
    fun tearDown() {
        harness.close()
    }

    @Test
    fun undo_restoresCervicalMucusAfterSingleChange() {
        runBlocking {
            viewModel.setCervicalMucus(CervicalMucusType.EGG_WHITE)
            awaitUntil { runBlocking { harness.ttcRepository.getTtcLogForDate(day)?.cervicalMucus != null } }
            viewModel.undoLastChange()
            awaitUntil { runBlocking { harness.ttcRepository.getTtcLogForDate(day)?.cervicalMucus == null } }
        }
    }

    @Test
    fun undo_restoresOvulationTest() {
        runBlocking {
            viewModel.setOvulationTest(OvulationTestResult.POSITIVE)
            awaitUntil { viewModel.uiState.value.ovulationTest == OvulationTestResult.POSITIVE }
            viewModel.undoLastChange()
            awaitUntil { viewModel.uiState.value.ovulationTest == null }
        }
    }

    @Test
    fun undo_restoresBbtAfterChange() {
        runBlocking {
            viewModel.onBbtChange("36.55")
            awaitUntil { viewModel.uiState.value.bbtInput == "36.55" }
            viewModel.undoLastChange()
            awaitUntil {
                viewModel.uiState.value.bbtInput
                    .isEmpty()
            }
        }
    }

    @Test
    fun undo_restoresDailyLogMedicationAndMood() {
        runBlocking {
            viewModel.toggleMood("آرام")
            viewModel.toggleMedication("ویتامین")
            viewModel.undoLastChange()
            awaitUntil {
                val log = harness.dailyLogFor(day)
                log != null && !log.medicationTags.contains("ویتامین")
            }
            viewModel.undoLastChange()
            awaitUntil {
                val log = harness.dailyLogFor(day)
                log != null && !log.moodTags.contains("آرام")
            }
        }
    }

    @Test
    fun undo_restoresPregnancyTest() {
        runBlocking {
            viewModel.setPregnancyTest(PregnancyTestResult.POSITIVE)
            awaitUntil { viewModel.uiState.value.pregnancyTest == PregnancyTestResult.POSITIVE }
            viewModel.undoLastChange()
            awaitUntil { viewModel.uiState.value.pregnancyTest == null }
        }
    }

    @Test
    fun undo_lifestyleSleepToggle_reverts() {
        runBlocking {
            harness.loggingPreferencesRepository.setLifestyleModuleEnabled(LogEditorCategory.SLEEP, true)
            viewModel.toggleLifestyleLogged(LogEditorCategory.SLEEP)
            awaitUntil { harness.dailyLogFor(day)?.sleepLogged == true }
            viewModel.undoLastChange()
            awaitUntil {
                val log = harness.dailyLogFor(day)
                log != null && !log.sleepLogged
            }
        }
    }

    private suspend fun awaitUntil(
        timeoutMs: Long = 6_000,
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
