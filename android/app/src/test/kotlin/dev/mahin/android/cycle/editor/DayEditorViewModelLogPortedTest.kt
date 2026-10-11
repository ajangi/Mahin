package dev.mahin.android.cycle.editor

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.entity.TtcDayLogEntity
import dev.mahin.core.model.PeriodFlowLevel
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DayEditorViewModelLogPortedTest {
    private lateinit var harness: DayEditorTestHarness
    private lateinit var viewModel: DayEditorViewModel

    @After
    fun tearDown() {
        harness.close()
    }

    @Test
    fun healthConnectOff_untickPeriod_doesNotDeletePeriodDay() {
        runBlocking {
            val day = LocalDate.of(2025, 5, 1)
            harness = DayEditorTestHarness.create(day)
            harness.seedCycleProfile()
            harness.cycleRepository.upsertPeriodDay(day, PeriodFlowLevel.LIGHT, hasClots = false)
            viewModel = harness.createViewModel(healthConnectActive = false)
            viewModel.openDate(day)
            awaitUntil { viewModel.uiState.value.periodLogged }
            viewModel.togglePeriodLogged()
            awaitWorkDone()
            assertThat(harness.periodDayFor(day)).isNotNull()
            assertThat(harness.tombstoneRepository.isUserDeleted(day)).isFalse()
        }
    }

    @Test
    fun healthConnectOn_untickPeriod_deletesRowAndWritesTombstone() {
        runBlocking {
            val day = LocalDate.of(2025, 5, 2)
            harness = DayEditorTestHarness.create(day)
            harness.seedCycleProfile()
            harness.cycleRepository.upsertPeriodDay(day, PeriodFlowLevel.MEDIUM, hasClots = false)
            viewModel = harness.createViewModel(healthConnectActive = true)
            viewModel.openDate(day)
            awaitUntil { viewModel.uiState.value.periodLogged }
            viewModel.togglePeriodLogged()
            awaitUntil { harness.periodDayFor(day) == null }
            awaitUntil { runBlocking { harness.tombstoneRepository.isUserDeleted(day) } }
            assertThat(harness.tombstoneRepository.isUserDeleted(day)).isTrue()
        }
    }

    @Test
    fun healthConnectOff_relogPeriod_clearsExistingTombstone() {
        runBlocking {
            val day = LocalDate.of(2025, 5, 4)
            harness = DayEditorTestHarness.create(day)
            harness.seedCycleProfile()
            harness.tombstoneRepository.markUserDeleted(day)
            viewModel = harness.createViewModel(healthConnectActive = false)
            viewModel.openDate(day)
            awaitUntil { viewModel.uiState.value.date == day }
            viewModel.togglePeriodLogged()
            awaitUntil {
                harness.periodDayFor(day) != null && viewModel.uiState.value.periodLogged
            }
            viewModel.setFlowLevel(PeriodFlowLevel.LIGHT)
            awaitUntil { harness.periodDayFor(day)?.flowLevel == PeriodFlowLevel.LIGHT }
            assertThat(harness.tombstoneRepository.isUserDeleted(day)).isFalse()
        }
    }

    @Test
    fun healthConnectOn_relogPeriod_clearsTombstone() {
        runBlocking {
            val day = LocalDate.of(2025, 5, 3)
            harness = DayEditorTestHarness.create(day)
            harness.seedCycleProfile()
            harness.tombstoneRepository.markUserDeleted(day)
            viewModel = harness.createViewModel(healthConnectActive = true)
            viewModel.openDate(day)
            awaitUntil { viewModel.uiState.value.date == day }
            viewModel.togglePeriodLogged()
            awaitUntil {
                harness.periodDayFor(day) != null && viewModel.uiState.value.periodLogged
            }
            viewModel.setFlowLevel(PeriodFlowLevel.HEAVY)
            awaitUntil { harness.periodDayFor(day)?.flowLevel == PeriodFlowLevel.HEAVY }
            assertThat(harness.tombstoneRepository.isUserDeleted(day)).isFalse()
        }
    }

    @Test
    fun onBbtChange_withInvalidPersianBbt_preservesStoredValue() {
        runBlocking {
            val day = LocalDate.of(2025, 3, 5)
            harness = DayEditorTestHarness.create(day)
            harness.seedCycleProfile(ReproductiveMode.TRYING_TO_CONCEIVE)
            harness.database.ttcDayLogDao().upsert(
                TtcDayLogEntity(
                    id = "t1",
                    logDate = day,
                    bbtCelsius = 36.6,
                    ovulationTestResult = null,
                    cervicalMucus = null,
                    intercourseLogged = false,
                    intercourseProtected = null,
                    pregnancyTestResult = null,
                    updatedAtEpochMs = 0L,
                ),
            )
            viewModel = harness.createViewModel()
            viewModel.openDate(day)
            awaitUntil { viewModel.uiState.value.date == day }
            viewModel.onBbtChange("۴۵")
            awaitUntil { viewModel.uiState.value.bbtError != null }
            assertThat(harness.ttcRepository.getTtcLogForDate(day)?.bbtCelsius).isEqualTo(36.6)
        }
    }

    @Test
    fun setNote_withIntercourseOptInOff_preservesStoredIntercourse() {
        runBlocking {
            val day = LocalDate.of(2025, 3, 8)
            harness = DayEditorTestHarness.create(day)
            harness.seedCycleProfile(ReproductiveMode.TRYING_TO_CONCEIVE)
            harness.database.ttcDayLogDao().upsert(
                TtcDayLogEntity(
                    id = "t2",
                    logDate = day,
                    bbtCelsius = null,
                    ovulationTestResult = null,
                    cervicalMucus = null,
                    intercourseLogged = true,
                    intercourseProtected = true,
                    pregnancyTestResult = null,
                    updatedAtEpochMs = 0L,
                ),
            )
            viewModel = harness.createViewModel()
            viewModel.openDate(day)
            awaitUntil { viewModel.uiState.value.intercourseLogged }
            assertThat(viewModel.uiState.value.sexualActivityEnabled).isFalse()
            viewModel.setNote("یادداشت")
            awaitUntil { harness.dailyLogFor(day)?.note == "یادداشت" }
            val stored = harness.ttcRepository.getTtcLogForDate(day)
            assertThat(stored?.intercourseLogged).isTrue()
            assertThat(stored?.intercourseProtected).isTrue()
        }
    }

    @Test
    fun toggleSymptom_beforeDateLoaded_isIgnored() {
        runBlocking {
            val day = LocalDate.of(2025, 6, 1)
            harness = DayEditorTestHarness.create(day)
            harness.seedCycleProfile()
            viewModel = harness.createViewModel()
            viewModel.toggleSymptom("blocked")
            assertThat(harness.dailyLogFor(day)).isNull()
            viewModel.openDate(day)
            awaitUntil { viewModel.uiState.value.date == day }
        }
    }

    private suspend fun awaitWorkDone() {
        repeat(20) {
            ShadowLooper.idleMainLooper()
        }
    }

    private suspend fun awaitUntil(
        timeoutMs: Long = 10_000,
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
