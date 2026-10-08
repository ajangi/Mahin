package dev.mahin.android.cycle

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.designsystem.LocalReducedMotion
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinCalendarDayDecoration
import dev.mahin.core.designsystem.component.MahinCalendarLegend
import dev.mahin.core.designsystem.component.MahinCalendarMarkerTints
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import java.time.LocalDate

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CycleCalendarScreen(
    modifier: Modifier = Modifier,
    onOpenLogForDate: (LocalDate) -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SharedTransitionLayout(modifier = modifier) {
        CycleCalendarScreenContent(
            state = state,
            sharedTransitionScope = this,
            onDateSelected = { viewModel.selectJalaliDate(it) },
            onDismissDaySheet = viewModel::dismissDaySheet,
            onToggleLegend = viewModel::toggleLegendExpanded,
            onJumpToToday = viewModel::jumpToToday,
            onVisibleMonthChanged = viewModel::onVisibleMonthChanged,
            onOpenLogForDate = onOpenLogForDate,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalComposeUiApi::class)
@Suppress("LongParameterList", "LongMethod")
@Composable
internal fun CycleCalendarScreenContent(
    state: CalendarUiState,
    onDateSelected: (JalaliDate) -> Unit,
    onDismissDaySheet: () -> Unit = {},
    onToggleLegend: () -> Unit = {},
    onJumpToToday: () -> Unit = {},
    onVisibleMonthChanged: (JalaliDate) -> Unit = {},
    onOpenLogForDate: (LocalDate) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    modifier: Modifier = Modifier,
) {
    val converter = PersianCivilDateConverter
    val today = LocalDate.now()
    val reducedMotion = LocalReducedMotion.current
    val selectedGregorian = converter.toGregorian(state.selectedJalali)
    val palette =
        CalendarMarkerPalette(
            periodLogged = MahinCalendarMarkerTints.periodLogged(),
            fertile = MahinCalendarMarkerTints.fertileWindow(),
            ovulation = MahinCalendarMarkerTints.estimatedOvulation(),
        )
    val scroll = rememberScrollState()
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("cycle_calendar_screen"),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(horizontal = MahinSpacing.md)
                    .then(
                        if (state.daySheetOpen) {
                            Modifier.semantics { invisibleToUser() }
                        } else {
                            Modifier
                        },
                    ),
        ) {
            Text(
                text = stringResource(R.string.calendar_legend_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MahinSpacing.sm, bottom = MahinSpacing.sm),
            )
            MahinCalendarLegend(
                expanded = state.legendExpanded,
                onToggleExpanded = onToggleLegend,
                modifier = Modifier.padding(bottom = MahinSpacing.sm),
            )
            MahinJalaliDatePicker(
                selectedDate = state.selectedJalali,
                onDateSelected = onDateSelected,
                converter = converter,
                visibleMonth = state.visibleMonth,
                onVisibleMonthChanged = onVisibleMonthChanged,
                dayDecoration = { date -> decorationFor(date, state.dayMarkers[date], today, palette) },
                dayCellModifier = { date ->
                    calendarDaySharedCellModifier(
                        date = date,
                        selectedGregorian = selectedGregorian,
                        daySheetOpen = state.daySheetOpen,
                        reducedMotion = reducedMotion,
                        sharedTransitionScope = sharedTransitionScope,
                    )
                },
                headerTrailing = {
                    TextButton(onClick = onJumpToToday) {
                        Text(text = stringResource(R.string.calendar_jump_today))
                    }
                },
            )
            Text(
                text = stringResource(R.string.prediction_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MahinSpacing.md, bottom = MahinSpacing.lg),
            )
        }
        if (sharedTransitionScope != null) {
            with(sharedTransitionScope) {
                CalendarAnimatedDaySheet(
                    open = state.daySheetOpen,
                    selectedJalali = state.selectedJalali,
                    selectedGregorian = selectedGregorian,
                    markers = state.dayMarkers[selectedGregorian],
                    logLines = state.dayLogs[selectedGregorian] ?: emptyList(),
                    onDismiss = onDismissDaySheet,
                    onEditLog = onOpenLogForDate,
                )
            }
        } else if (state.daySheetOpen) {
            CalendarDaySheet(
                open = true,
                selectedJalali = state.selectedJalali,
                markers = state.dayMarkers[selectedGregorian],
                logLines = state.dayLogs[selectedGregorian] ?: emptyList(),
                onDismiss = onDismissDaySheet,
                onEditLog = onOpenLogForDate,
            )
        }
    }
}

data class DayMarkers(
    val loggedPeriod: Boolean = false,
    val predictedPeriod: Boolean = false,
    val fertileWindow: Boolean = false,
    val estimatedOvulation: Boolean = false,
    val hasLogEntries: Boolean = false,
)

internal data class CalendarMarkerPalette(
    val periodLogged: androidx.compose.ui.graphics.Color,
    val fertile: androidx.compose.ui.graphics.Color,
    val ovulation: androidx.compose.ui.graphics.Color,
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun calendarDaySharedCellModifier(
    date: LocalDate,
    selectedGregorian: LocalDate,
    daySheetOpen: Boolean,
    reducedMotion: Boolean,
    sharedTransitionScope: SharedTransitionScope?,
): Modifier {
    if (reducedMotion || !daySheetOpen || date != selectedGregorian) return Modifier
    val scope = LocalCalendarDaySheetTransitionScope.current ?: return Modifier
    val stScope = sharedTransitionScope ?: return Modifier
    return with(stScope) {
        Modifier.sharedBounds(
            sharedContentState = rememberSharedContentState(calendarDaySharedContentKey(date)),
            animatedVisibilityScope = scope,
        )
    }
}

internal fun decorationFor(
    date: LocalDate,
    markers: DayMarkers?,
    today: LocalDate,
    palette: CalendarMarkerPalette,
): MahinCalendarDayDecoration? {
    if (markers == null && date != today) return null
    val m = markers ?: DayMarkers()
    val fill =
        when {
            m.loggedPeriod -> palette.periodLogged
            m.estimatedOvulation -> palette.ovulation
            m.fertileWindow -> palette.fertile
            else -> null
        }
    return MahinCalendarDayDecoration(
        fillColor = fill,
        predictedPeriodOutline = m.predictedPeriod,
        estimatedOvulation = m.estimatedOvulation,
        hasLogEntries = m.hasLogEntries,
        isToday = date == today,
    )
}
