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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
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
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
            },
        )

    @Before
    fun drainMainFromPriorTests() {
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()
    }

    private suspend fun TestScope.awaitTtcBbtPersisted(
        harness: DayEditorTestHarness,
        day: LocalDate,
        bbt: Double,
    ) {
        repeat(30) {
            advanceUntilIdle()
            ShadowLooper.idleMainLooper()
            if (harness.ttcRepository.getTtcLogForDate(day)?.bbtCelsius == bbt) return
        }
        throw AssertionError("Expected BBT $bbt for $day")
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
                viewModel.undoLastChange()
                viewModel.uiState.filter { it.cervicalMucus == null }.first()
                assertThat(harness.ttcRepository.getTtcLogForDate(day)?.cervicalMucus).isNull()
            }
        }

    @Test
    fun undo_restoresOvulationTestInDatabase() =
        runTest(testDispatcher) {
            withTtcEditor { harness, viewModel ->
                viewModel.setOvulationTest(OvulationTestResult.POSITIVE)
                viewModel.uiState.filter { it.ovulationTest == OvulationTestResult.POSITIVE }.first()
                viewModel.undoLastChange()
                viewModel.uiState.filter { it.ovulationTest == null }.first()
                assertThat(harness.ttcRepository.getTtcLogForDate(day)?.ovulationTestResult).isNull()
            }
        }

    @Test
    fun undo_restoresBbtInDatabase() =
        runTest(testDispatcher) {
            withTtcEditor { harness, viewModel ->
                viewModel.onBbtChange("36.55")
                viewModel.uiState.filter { it.bbtInput == "36.55" }.first()
                viewModel.onBbtChange("36.56")
                viewModel.uiState.filter { it.bbtInput == "36.56" }.first()
                viewModel.undoLastChange()
                viewModel.uiState.filter { it.bbtInput == "36.55" }.first()
                awaitTtcBbtPersisted(harness, day, 36.55)
            }
        }

    @Test
    fun undo_restoresBbtAfterRapidChangesWithoutWaitingBetweenCalls() =
        runTest(testDispatcher) {
            withTtcEditor { harness, viewModel ->
                viewModel.onBbtChange("36.55")
                viewModel.onBbtChange("36.56")
                viewModel.uiState.filter { it.bbtInput == "36.56" }.first()
                viewModel.undoLastChange()
                viewModel.uiState.filter { it.bbtInput == "36.55" }.first()
                awaitTtcBbtPersisted(harness, day, 36.55)
            }
        }

    @Test
    fun undo_restoresDailyLogMedicationThenMood_withSecondUndoNoOp() =
        runTest(testDispatcher) {
            withTtcEditor { harness, viewModel ->
                viewModel.toggleMood("آرام")
                viewModel.uiState.filter { it.moodTags.contains("آرام") }.first()
                viewModel.toggleMedication("ویتامین")
                viewModel.uiState.filter { it.medicationTags.contains("ویتامین") }.first()
                viewModel.undoLastChange()
                viewModel.uiState
                    .filter {
                        !it.medicationTags.contains("ویتامین") && it.moodTags.contains("آرام")
                    }.first()
                viewModel.uiState.filter { it.undoMessage == null }.first()
                val moodAfterFirstUndo = harness.dailyLogFor(day)?.moodTags
                assertThat(viewModel.uiState.value.undoMessage).isNull()
                viewModel.undoLastChange()
                viewModel.uiState.filter { !it.medicationTags.contains("ویتامین") }.first()
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
                viewModel.undoLastChange()
                viewModel.uiState.filter { it.pregnancyTest == null }.first()
                assertThat(harness.ttcRepository.getTtcLogForDate(day)?.pregnancyTestResult).isNull()
            }
        }

    @Test
    fun undo_lifestyleSleepToggle_revertsInDatabase() =
        runTest(testDispatcher) {
            withTtcEditor { harness, viewModel ->
                harness.loggingPreferencesRepository.setLifestyleModuleEnabled(LogEditorCategory.SLEEP, true)
                viewModel.toggleLifestyleLogged(LogEditorCategory.SLEEP)
                viewModel.uiState.filter { it.sleepLogged }.first()
                viewModel.undoLastChange()
                viewModel.uiState.filter { !it.sleepLogged }.first()
                assertThat(harness.dailyLogFor(day)?.sleepLogged).isFalse()
            }
        }
}
