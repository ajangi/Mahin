package dev.mahin.android.cycle.editor

import java.time.LocalDate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
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
