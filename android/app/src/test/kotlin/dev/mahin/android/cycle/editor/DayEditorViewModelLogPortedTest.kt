package dev.mahin.android.cycle.editor

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.entity.TtcDayLogEntity
import dev.mahin.core.model.PeriodFlowLevel
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
class DayEditorViewModelLogPortedTest {
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule =
        MainDispatcherRule(
            testDispatcher,
            beforeResetMain = {
                testDispatcher.scheduler.advanceUntilIdle()
                DayEditorTestVmRegistry.clearAll()
                ShadowLooper.idleMainLooper()
            },
        )

    @Before
    fun drainMainFromPriorTests() {
        testDispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()
    }

    @Test
    fun healthConnectOff_untickPeriod_doesNotDeletePeriodDay() =
        runTest(testDispatcher) {
            val day = LocalDate.of(2025, 5, 1)
            val testHarness = DayEditorTestHarness.create(day)
            try {
                testHarness.seedCycleProfile()
                testHarness.cycleRepository.upsertPeriodDay(day, PeriodFlowLevel.LIGHT, hasClots = false)
                val viewModel = testHarness.createViewModel(healthConnectActive = false)
                viewModel.openDateAndAwait(day)
                viewModel.uiState.filter { it.periodLogged }.first()
                viewModel.togglePeriodLogged()
                viewModel.uiState.filter { it.undoMessage != null }.first()
                assertThat(testHarness.periodDayFor(day)).isNotNull()
                assertThat(testHarness.tombstoneRepository.isUserDeleted(day)).isFalse()
            } finally {
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
                testHarness.close()
            }
        }

    @Test
    fun healthConnectOn_untickPeriod_deletesRowAndWritesTombstone() =
        runTest(testDispatcher) {
            val day = LocalDate.of(2025, 5, 2)
            val testHarness = DayEditorTestHarness.create(day)
            try {
                testHarness.seedCycleProfile()
                testHarness.cycleRepository.upsertPeriodDay(day, PeriodFlowLevel.MEDIUM, hasClots = false)
                val viewModel = testHarness.createViewModel(healthConnectActive = true)
                viewModel.openDateAndAwait(day)
                viewModel.uiState.filter { it.periodLogged }.first()
                viewModel.togglePeriodLogged()
                viewModel.uiState.filter { !it.periodLogged }.first()
                testHarness.awaitNoPeriodDay(day)
                testHarness.tombstoneRepository.awaitUserDeleted(day, deleted = true)
                assertThat(testHarness.tombstoneRepository.isUserDeleted(day)).isTrue()
            } finally {
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
                testHarness.close()
            }
        }

    @Test
    fun healthConnectOff_relogPeriod_clearsExistingTombstone() =
        runTest(testDispatcher) {
            val day = LocalDate.of(2025, 5, 4)
            val testHarness = DayEditorTestHarness.create(day)
            try {
                testHarness.seedCycleProfile()
                testHarness.tombstoneRepository.markUserDeleted(day)
                val viewModel = testHarness.createViewModel(healthConnectActive = false)
                viewModel.openDateAndAwait(day)
                viewModel.togglePeriodLogged()
                viewModel.uiState.filter { it.periodLogged }.first()
                viewModel.setFlowLevel(PeriodFlowLevel.LIGHT)
                viewModel.uiState.filter { it.flowLevel == PeriodFlowLevel.LIGHT }.first()
                testHarness.awaitPeriodDayFlowLevel(day, PeriodFlowLevel.LIGHT)
                assertThat(testHarness.periodDayFor(day)).isNotNull()
                testHarness.tombstoneRepository.awaitUserDeleted(day, deleted = false)
                assertThat(testHarness.tombstoneRepository.isUserDeleted(day)).isFalse()
            } finally {
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
                testHarness.close()
            }
        }

    @Test
    fun healthConnectOn_relogPeriod_clearsTombstone() =
        runTest(testDispatcher) {
            val day = LocalDate.of(2025, 5, 3)
            val testHarness = DayEditorTestHarness.create(day)
            try {
                testHarness.seedCycleProfile()
                testHarness.tombstoneRepository.markUserDeleted(day)
                val viewModel = testHarness.createViewModel(healthConnectActive = true)
                viewModel.openDateAndAwait(day)
                viewModel.togglePeriodLogged()
                viewModel.uiState.filter { it.periodLogged }.first()
                viewModel.setFlowLevel(PeriodFlowLevel.HEAVY)
                viewModel.uiState.filter { it.flowLevel == PeriodFlowLevel.HEAVY }.first()
                testHarness.awaitPeriodDayFlowLevel(day, PeriodFlowLevel.HEAVY)
                assertThat(testHarness.periodDayFor(day)?.flowLevel).isEqualTo(PeriodFlowLevel.HEAVY)
                testHarness.tombstoneRepository.awaitUserDeleted(day, deleted = false)
                assertThat(testHarness.tombstoneRepository.isUserDeleted(day)).isFalse()
            } finally {
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
                testHarness.close()
            }
        }

    @Test
    fun onBbtChange_withInvalidPersianBbt_preservesStoredValue() =
        runTest(testDispatcher) {
            val day = LocalDate.of(2025, 3, 5)
            val testHarness = DayEditorTestHarness.create(day)
            try {
                testHarness.seedCycleProfile(ReproductiveMode.TRYING_TO_CONCEIVE)
                testHarness.database.ttcDayLogDao().upsert(
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
                val viewModel = testHarness.createViewModel()
                viewModel.openDateAndAwait(day)
                viewModel.onBbtChange("۴۵")
                viewModel.uiState.filter { it.bbtError != null }.first()
                assertThat(testHarness.ttcRepository.getTtcLogForDate(day)?.bbtCelsius).isEqualTo(36.6)
            } finally {
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
                testHarness.close()
            }
        }

    @Test
    fun setNote_withIntercourseOptInOff_preservesStoredIntercourse() =
        runTest(testDispatcher) {
            val day = LocalDate.of(2025, 3, 8)
            val testHarness = DayEditorTestHarness.create(day)
            try {
                testHarness.seedCycleProfile(ReproductiveMode.TRYING_TO_CONCEIVE)
                testHarness.database.ttcDayLogDao().upsert(
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
                val viewModel = testHarness.createViewModel()
                viewModel.openDateAndAwait(day)
                viewModel.uiState.filter { it.intercourseLogged }.first()
                assertThat(viewModel.uiState.value.sexualActivityEnabled).isFalse()
                viewModel.setNote("یادداشت")
                viewModel.uiState.filter { it.note == "یادداشت" }.first()
                testHarness.awaitDailyLog(day) { it.note == "یادداشت" }
                val stored = testHarness.ttcRepository.getTtcLogForDate(day)
                assertThat(stored?.intercourseLogged).isTrue()
                assertThat(stored?.intercourseProtected).isTrue()
            } finally {
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
                testHarness.close()
            }
        }

    @Test
    fun toggleSymptom_beforeDateLoaded_isIgnored() =
        runTest(testDispatcher) {
            val day = LocalDate.of(2025, 6, 1)
            val testHarness = DayEditorTestHarness.create(day)
            try {
                testHarness.seedCycleProfile()
                val viewModel = testHarness.createViewModel()
                viewModel.toggleSymptom("blocked")
                assertThat(testHarness.dailyLogFor(day)).isNull()
                viewModel.openDateAndAwait(day)
            } finally {
                testDispatcher.scheduler.advanceUntilIdle()
                ShadowLooper.idleMainLooper()
                testHarness.close()
            }
        }
}
