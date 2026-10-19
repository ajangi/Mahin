package dev.mahin.android.cycle.editor

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.LogEditorCategory
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PregnancyTestResult
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.testing.MainDispatcherRule
import java.time.LocalDate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DayEditorViewModelUndoTest {
    private val testDispatcher = StandardTestDispatcher()
    private val day = LocalDate.of(2025, 6, 15)

    @get:Rule
    val mainDispatcherRule =
        MainDispatcherRule(
            testDispatcher,
            beforeResetMain = {
                DayEditorTestVmRegistry.scheduler = testDispatcher.scheduler
                testDispatcher.scheduler.advanceUntilIdle()
                DayEditorTestVmRegistry.clearAll()
            },
        )

    @Before
    fun drainMainFromPriorTests() {
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()
    }

    private suspend fun withTtcEditor(block: suspend (DayEditorTestHarness, DayEditorViewModel) -> Unit) {
        val harness = DayEditorTestHarness.create(day)
        try {
            harness.seedCycleProfile(ReproductiveMode.TRYING_TO_CONCEIVE)
            val viewModel = harness.createViewModel()
            harness.loggingPreferencesRepository.setSexualActivityEnabled(true)
            viewModel.openDateAndAwait(day)
            block(harness, viewModel)
        } finally {
            testDispatcher.scheduler.advanceUntilIdle()
            ShadowLooper.idleMainLooper()
            harness.close()
        }
    }

    @Test
    fun undo_restoresCervicalMucusAfterSingleChange() =
        runTest(testDispatcher) {
            withTtcEditor { harness, viewModel ->
                viewModel.setCervicalMucus(CervicalMucusType.EGG_WHITE)
                viewModel.uiState.filter { it.cervicalMucus == CervicalMucusType.EGG_WHITE }.first()
                harness.awaitTtcCervicalMucus(day, CervicalMucusType.EGG_WHITE)
                viewModel.undoLastChange()
                viewModel.uiState.filter { it.cervicalMucus == null }.first()
                harness.awaitTtcCervicalMucus(day, null)
            }
        }

    @Test
    fun undo_restoresOvulationTestInDatabase() =
        runTest(testDispatcher) {
            withTtcEditor { harness, viewModel ->
                viewModel.setOvulationTest(OvulationTestResult.POSITIVE)
                viewModel.uiState.filter { it.ovulationTest == OvulationTestResult.POSITIVE }.first()
                harness.awaitTtcOvulationTest(day, OvulationTestResult.POSITIVE)
                viewModel.undoLastChange()
                viewModel.uiState.filter { it.ovulationTest == null }.first()
                harness.awaitTtcOvulationTest(day, null)
            }
        }

    @Test
    fun undo_restoresBbtInDatabase() =
        runTest(testDispatcher) {
            withTtcEditor { harness, viewModel ->
                viewModel.onBbtChange("36.55")
                viewModel.uiState.filter { it.bbtInput == "36.55" }.first()
                harness.awaitTtcBbt(day, 36.55)
                viewModel.onBbtChange("36.56")
                viewModel.uiState.filter { it.bbtInput == "36.56" }.first()
                harness.awaitTtcBbt(day, 36.56)
                viewModel.undoLastChange()
                viewModel.uiState.filter { it.bbtInput == "36.55" }.first()
                harness.awaitTtcBbt(day, 36.55)
            }
        }

    @Test
    fun undo_restoresBbtAfterRapidChangesWithoutWaitingBetweenCalls() =
        runTest(testDispatcher) {
            withTtcEditor { harness, viewModel ->
                viewModel.onBbtChange("36.55")
                viewModel.onBbtChange("36.56")
                viewModel.uiState.filter { it.bbtInput == "36.56" }.first()
                harness.awaitTtcBbt(day, 36.56)
                viewModel.undoLastChange()
                viewModel.uiState.filter { it.bbtInput == "36.55" }.first()
                harness.awaitTtcBbt(day, 36.55)
            }
        }

    @Test
    fun undo_restoresDailyLogMedicationThenMood_withSecondUndoNoOp() =
        runTest(testDispatcher) {
            withTtcEditor { harness, viewModel ->
                viewModel.toggleMood("آرام")
                viewModel.uiState.filter { it.moodTags.contains("آرام") }.first()
                harness.awaitDailyLog(day) { it.moodTags.contains("آرام") }
                viewModel.toggleMedication("ویتامین")
                viewModel.uiState.filter { it.medicationTags.contains("ویتامین") }.first()
                harness.awaitDailyLog(day) { it.medicationTags.contains("ویتامین") }
                viewModel.undoLastChange()
                viewModel.uiState
                    .filter {
                        !it.medicationTags.contains("ویتامین") && it.moodTags.contains("آرام")
                    }.first()
                harness.awaitDailyLog(day) {
                    !it.medicationTags.contains("ویتامین") && it.moodTags.contains("آرام")
                }
                viewModel.uiState.filter { it.undoMessage == null }.first()
                val moodAfterFirstUndo = harness.dailyLogFor(day)?.moodTags
                assertThat(viewModel.uiState.value.undoMessage).isNull()
                viewModel.undoLastChange()
                harness.awaitDailyLog(day) {
                    it.moodTags == moodAfterFirstUndo && !it.medicationTags.contains("ویتامین")
                }
                assertThat(harness.dailyLogFor(day)?.moodTags).isEqualTo(moodAfterFirstUndo)
                assertThat(harness.dailyLogFor(day)?.medicationTags).doesNotContain("ویتامین")
            }
        }

    @Test
    fun undo_restoresPregnancyTestInDatabase() =
        runTest(testDispatcher) {
            withTtcEditor { harness, viewModel ->
                viewModel.setPregnancyTest(PregnancyTestResult.POSITIVE)
                viewModel.uiState.filter { it.pregnancyTest == PregnancyTestResult.POSITIVE }.first()
                harness.awaitTtcPregnancyTest(day, PregnancyTestResult.POSITIVE)
                viewModel.undoLastChange()
                viewModel.uiState.filter { it.pregnancyTest == null }.first()
                harness.awaitTtcPregnancyTest(day, null)
            }
        }

    @Test
    fun undo_lifestyleSleepToggle_revertsInDatabase() =
        runTest(testDispatcher) {
            withTtcEditor { harness, viewModel ->
                harness.loggingPreferencesRepository.setLifestyleModuleEnabled(LogEditorCategory.SLEEP, true)
                viewModel.toggleLifestyleLogged(LogEditorCategory.SLEEP)
                viewModel.uiState.filter { it.sleepLogged }.first()
                harness.awaitDailyLog(day) { it.sleepLogged }
                viewModel.undoLastChange()
                viewModel.uiState.filter { !it.sleepLogged }.first()
                harness.awaitDailyLog(day) { !it.sleepLogged }
            }
        }
}
