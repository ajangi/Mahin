package dev.mahin.android.cycle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.designsystem.LocalMahinExtendedColors
import dev.mahin.core.designsystem.MahinExtendedColors
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinCalendarLegend
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import dev.mahin.core.designsystem.component.MahinScreenHeader

@Composable
fun CycleCalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CycleCalendarScreenContent(
        state = state,
        onDateSelected = { viewModel.selectJalaliDate(it) },
        modifier = modifier,
    )
}

@Composable
internal fun CycleCalendarScreenContent(
    state: CalendarUiState,
    onDateSelected: (JalaliDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val converter = PersianCivilDateConverter
    val extended = LocalMahinExtendedColors.current
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("cycle_calendar_screen")
                .verticalScroll(rememberScrollState())
                .padding(MahinSpacing.md),
    ) {
        MahinScreenHeader(
            title = stringResource(R.string.calendar_title),
            subtitle = stringResource(R.string.calendar_legend_hint),
        )
        MahinCalendarLegend(modifier = Modifier.padding(vertical = MahinSpacing.md))
        MahinJalaliDatePicker(
            selectedDate = state.selectedJalali,
            onDateSelected = onDateSelected,
            converter = converter,
            initialVisibleMonth = state.selectedJalali,
            dayBackgroundColor = { date -> markerColor(state.dayMarkers[date], extended) },
        )
        Text(
            text = stringResource(R.string.prediction_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MahinSpacing.md),
        )
    }
}

private fun markerColor(
    markers: DayMarkers?,
    extended: MahinExtendedColors,
): Color? {
    if (markers == null) return null
    return when {
        markers.loggedPeriod -> extended.healthPeriod.copy(alpha = 0.45f)
        markers.predictedPeriod -> extended.healthPeriod.copy(alpha = 0.2f)
        markers.fertileWindow -> extended.healthFertility.copy(alpha = 0.25f)
        else -> null
    }
}

data class DayMarkers(
    val loggedPeriod: Boolean = false,
    val predictedPeriod: Boolean = false,
    val fertileWindow: Boolean = false,
)
