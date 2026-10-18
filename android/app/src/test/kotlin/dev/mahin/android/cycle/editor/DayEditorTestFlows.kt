package dev.mahin.android.cycle.editor

import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.database.entity.TtcDayLogEntity
import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PeriodFlowLevel
import dev.mahin.core.model.PregnancyTestResult
import java.time.LocalDate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.robolectric.shadows.ShadowLooper

internal suspend fun DayEditorViewModel.openDateAndAwait(day: LocalDate) {
    openDate(day)
    uiState.filter { it.date == day }.first()
}

internal suspend fun DayEditorViewModel.awaitUiState(predicate: (DayEditorUiState) -> Boolean) {
    uiState.filter(predicate).first()
}

internal suspend fun DayEditorTestHarness.awaitDailyLog(
    day: LocalDate,
    predicate: (DailyLogEntity) -> Boolean,
): DailyLogEntity =
    database
        .dailyLogDao()
        .observeForDate(day)
        .filter { it != null && predicate(it) }
        .map { it!! }
        .first()

internal suspend fun DayEditorTestHarness.awaitPeriodDayFlowLevel(
    day: LocalDate,
    level: PeriodFlowLevel,
) {
    database
        .periodDayDao()
        .observeRange(day, day)
        .filter { days -> days.firstOrNull()?.flowLevel == level }
        .first()
}

internal suspend fun DayEditorTestHarness.awaitTtcLog(
    day: LocalDate,
    predicate: (TtcDayLogEntity?) -> Boolean,
) {
    database
        .ttcDayLogDao()
        .observeRange(day, day)
        .map { rows -> rows.firstOrNull() }
        .filter { predicate(it) }
        .first()
}

internal suspend fun DayEditorTestHarness.awaitTtcCervicalMucus(
    day: LocalDate,
    mucus: CervicalMucusType?,
) {
    awaitTtcLog(day) { it?.cervicalMucus == mucus }
}

internal suspend fun DayEditorTestHarness.awaitTtcOvulationTest(
    day: LocalDate,
    result: OvulationTestResult?,
) {
    awaitTtcLog(day) { it?.ovulationTestResult == result }
}

internal suspend fun DayEditorTestHarness.awaitTtcBbt(
    day: LocalDate,
    bbt: Double?,
) {
    awaitTtcLog(day) { it?.bbtCelsius == bbt }
}

internal suspend fun DayEditorTestHarness.awaitTtcPregnancyTest(
    day: LocalDate,
    result: PregnancyTestResult?,
) {
    awaitTtcLog(day) { it?.pregnancyTestResult == result }
}

internal suspend fun TestScope.awaitUserDeleted(
    harness: DayEditorTestHarness,
    day: LocalDate,
) {
    repeat(50) {
        advanceUntilIdle()
        ShadowLooper.idleMainLooper()
        if (harness.tombstoneRepository.isUserDeleted(day)) return
    }
    throw AssertionError("Expected tombstone for $day")
}
