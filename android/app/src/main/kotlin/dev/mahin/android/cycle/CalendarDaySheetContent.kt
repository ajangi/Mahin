package dev.mahin.android.cycle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.datetime.PersianDigits
import dev.mahin.core.designsystem.LocalReducedMotion
import dev.mahin.core.designsystem.MahinSpacing
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDaySheet(
    open: Boolean,
    selectedJalali: JalaliDate,
    markers: DayMarkers?,
    onDismiss: () -> Unit,
    onEditLog: (LocalDate) -> Unit,
) {
    if (!open) return
    val converter = PersianCivilDateConverter
    val gregorian = converter.toGregorian(selectedJalali)
    val skipHalfExpanded = LocalReducedMotion.current
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = skipHalfExpanded,
        )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        CalendarDaySheetContent(
            jalali = selectedJalali,
            gregorian = gregorian,
            markers = markers,
            onEditLog = { onEditLog(gregorian) },
            modifier = Modifier.testTag("calendar_day_sheet"),
        )
    }
}

@Composable
internal fun CalendarDaySheetContent(
    jalali: JalaliDate,
    gregorian: LocalDate,
    markers: DayMarkers?,
    onEditLog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(MahinSpacing.lg)) {
        Text(
            text =
                stringResource(
                    R.string.calendar_day_sheet_title,
                    PersianDigits.format(jalali.day),
                    PersianDigits.format(jalali.year),
                ),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text =
                stringResource(
                    R.string.calendar_day_sheet_gregorian,
                    PersianDigits.format(gregorian.format(DateTimeFormatter.ISO_LOCAL_DATE)),
                ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = dayPredictionCopy(markers),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = MahinSpacing.md),
        )
        if (markers?.fertileWindow == true || markers?.estimatedOvulation == true) {
            Text(
                text = stringResource(R.string.today_fertile_not_contraception),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                    Modifier
                        .padding(top = MahinSpacing.sm)
                        .testTag("fertile_not_contraception_copy"),
            )
        }
        Button(
            onClick = onEditLog,
            modifier = Modifier.padding(top = MahinSpacing.lg),
        ) {
            Text(text = stringResource(R.string.calendar_day_sheet_edit))
        }
    }
}

@Composable
private fun dayPredictionCopy(markers: DayMarkers?): String {
    if (markers == null) return stringResource(R.string.calendar_day_sheet_no_markers)
    return when {
        markers.loggedPeriod -> stringResource(R.string.calendar_day_sheet_logged_period)
        markers.predictedPeriod -> stringResource(R.string.calendar_day_sheet_predicted_period)
        markers.estimatedOvulation -> stringResource(R.string.calendar_day_sheet_ovulation)
        markers.fertileWindow -> stringResource(R.string.calendar_day_sheet_fertile)
        else -> stringResource(R.string.calendar_day_sheet_neutral)
    }
}
