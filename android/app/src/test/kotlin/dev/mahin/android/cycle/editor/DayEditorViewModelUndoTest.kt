package dev.mahin.android.cycle.editor

import com.google.common.truth.Truth.assertThat
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
            awaitUntilDb { harness.ttcRepository.getTtcLogForDate(day)?.cervicalMucus == CervicalMucusType.EGG_WHITE }
            viewModel.undoLastChange()
            awaitUntilDb { harness.ttcRepository.getTtcLogForDate(day)?.cervicalMucus == null }
        }
    }

    @Test
    fun undo_restoresOvulationTestInDatabase() {
        runBlocking {
            viewModel.setOvulationTest(OvulationTestResult.POSITIVE)
            awaitUntilDb {
                harness.ttcRepository.getTtcLogForDate(day)?.ovulationTestResult ==
                    OvulationTestResult.POSITIVE
            }
            viewModel.undoLastChange()
            awaitUntilDb { harness.ttcRepository.getTtcLogForDate(day)?.ovulationTestResult == null }
        }
    }

    @Test
    fun undo_restoresBbtInDatabase() {
        runBlocking {
            viewModel.onBbtChange("36.55")
            awaitUntilDb { harness.ttcRepository.getTtcLogForDate(day)?.bbtCelsius == 36.55 }
            viewModel.onBbtChange("36.56")
            awaitUntilDb { harness.ttcRepository.getTtcLogForDate(day)?.bbtCelsius == 36.56 }
            viewModel.undoLastChange()
            awaitUntilDb { harness.ttcRepository.getTtcLogForDate(day)?.bbtCelsius == 36.55 }
        }
    }

    @Test
    fun undo_restoresBbtAfterRapidChangesWithoutWaitingBetweenCalls() {
        runBlocking {
            viewModel.onBbtChange("36.55")
            viewModel.onBbtChange("36.56")
            awaitUntilDb { harness.ttcRepository.getTtcLogForDate(day)?.bbtCelsius == 36.56 }
            viewModel.undoLastChange()
            awaitUntilDb { harness.ttcRepository.getTtcLogForDate(day)?.bbtCelsius == 36.55 }
        }
    }

    @Test
    fun undo_restoresDailyLogMedicationThenMood_withSecondUndoNoOp() {
        runBlocking {
            viewModel.toggleMood("آرام")
            awaitUntilDb {
                val log = harness.dailyLogFor(day)
                log != null && log.moodTags.contains("آرام")
            }
            awaitUntil {
                viewModel.uiState.value.moodTags
                    .contains("آرام")
            }
            viewModel.toggleMedication("ویتامین")
            awaitUntilDb {
                val log = harness.dailyLogFor(day)
                log != null && log.medicationTags.contains("ویتامین")
            }
            viewModel.undoLastChange()
            awaitUntilDb {
                val log = harness.dailyLogFor(day)
                log != null && !log.medicationTags.contains("ویتامین") && log.moodTags.contains("آرام")
            }
            awaitUntil { viewModel.uiState.value.undoMessage == null }
            val moodAfterFirstUndo = harness.dailyLogFor(day)?.moodTags
            assertThat(viewModel.uiState.value.undoMessage).isNull()
            viewModel.undoLastChange()
            assertThat(harness.dailyLogFor(day)?.moodTags).isEqualTo(moodAfterFirstUndo)
            assertThat(harness.dailyLogFor(day)?.medicationTags).doesNotContain("ویتامین")
        }
    }

    @Test
    fun undo_restoresPregnancyTestInDatabase() {
        runBlocking {
            viewModel.setPregnancyTest(PregnancyTestResult.POSITIVE)
            awaitUntilDb {
                harness.ttcRepository.getTtcLogForDate(day)?.pregnancyTestResult ==
                    PregnancyTestResult.POSITIVE
            }
            viewModel.undoLastChange()
            awaitUntilDb { harness.ttcRepository.getTtcLogForDate(day)?.pregnancyTestResult == null }
        }
    }

    @Test
    fun undo_lifestyleSleepToggle_revertsInDatabase() {
        runBlocking {
            harness.loggingPreferencesRepository.setLifestyleModuleEnabled(LogEditorCategory.SLEEP, true)
            viewModel.toggleLifestyleLogged(LogEditorCategory.SLEEP)
            awaitUntilDb { harness.dailyLogFor(day)?.sleepLogged == true }
            viewModel.undoLastChange()
            awaitUntilDb {
                val log = harness.dailyLogFor(day)
                log != null && !log.sleepLogged
            }
        }
    }

    private suspend fun awaitUntil(
        timeoutMs: Long = 10_000,
        condition: () -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            ShadowLooper.idleMainLooper()
            if (condition()) return
        }
        throw AssertionError("Condition not met within ${timeoutMs}ms")
    }

    private suspend fun awaitUntilDb(condition: suspend () -> Boolean) {
        val deadline = System.currentTimeMillis() + 10_000
        while (System.currentTimeMillis() < deadline) {
            ShadowLooper.idleMainLooper()
            if (condition()) return
        }
        throw AssertionError("DB condition not met within 10000ms")
    }
}
