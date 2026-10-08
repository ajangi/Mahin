package dev.mahin.android.cycle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinCalendarDayDecoration
import dev.mahin.core.designsystem.component.MahinCalendarLegend
import dev.mahin.core.designsystem.component.MahinCalendarMarkerTints
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import java.time.LocalDate

@Composable
fun CycleCalendarScreen(
    modifier: Modifier = Modifier,
    onOpenLogForDate: (LocalDate) -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CycleCalendarScreenContent(
        state = state,
        onDateSelected = { viewModel.selectJalaliDate(it) },
        onDismissDaySheet = viewModel::dismissDaySheet,
        onToggleLegend = viewModel::toggleLegendExpanded,
        onJumpToToday = viewModel::jumpToToday,
        onOpenLogForDate = onOpenLogForDate,
        modifier = modifier,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun CycleCalendarScreenContent(
    state: CalendarUiState,
    onDateSelected: (JalaliDate) -> Unit,
    onDismissDaySheet: () -> Unit = {},
    onToggleLegend: () -> Unit = {},
    onJumpToToday: () -> Unit = {},
    onOpenLogForDate: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val converter = PersianCivilDateConverter
    val today = LocalDate.now()
    val palette =
        CalendarMarkerPalette(
            periodLogged = MahinCalendarMarkerTints.periodLogged(),
            fertile = MahinCalendarMarkerTints.fertileWindow(),
            ovulation = MahinCalendarMarkerTints.estimatedOvulation(),
        )
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("cycle_calendar_screen")
                .padding(horizontal = MahinSpacing.md),
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
            initialVisibleMonth = state.visibleMonth,
            dayDecoration = { date -> decorationFor(date, state.dayMarkers[date], today, palette) },
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
    CalendarDaySheet(
        open = state.daySheetOpen,
        selectedJalali = state.selectedJalali,
        markers = state.dayMarkers[converter.toGregorian(state.selectedJalali)],
        onDismiss = onDismissDaySheet,
        onEditLog = onOpenLogForDate,
    )
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
            m.fertileWindow -> palette.fertile
            m.estimatedOvulation -> palette.ovulation
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
