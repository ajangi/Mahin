package dev.mahin.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import dev.mahin.core.datetime.CivilDateConverter
import dev.mahin.core.datetime.JalaliCalendar
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.datetime.PersianDigits
import dev.mahin.core.designsystem.MahinRadius
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.R
import dev.mahin.core.designsystem.mahinMinimumTouchTarget
import dev.mahin.core.designsystem.mahinTextStyle
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("LongParameterList")
@Composable
fun MahinJalaliDatePicker(
    selectedDate: JalaliDate?,
    onDateSelected: (JalaliDate) -> Unit,
    modifier: Modifier = Modifier,
    converter: CivilDateConverter = PersianCivilDateConverter,
    initialVisibleMonth: JalaliDate = selectedDate ?: converter.toJalali(LocalDate.now()),
    dayBackgroundColor: (LocalDate) -> Color? = { null },
) {
    var visibleYear by remember(initialVisibleMonth) { mutableStateOf(initialVisibleMonth.year) }
    var visibleMonth by remember(initialVisibleMonth) { mutableStateOf(initialVisibleMonth.month) }
    val monthNames = stringArrayResource(R.array.ds_jalali_month_names)
    Column(
        modifier = modifier.fillMaxWidth().padding(MahinSpacing.md),
        verticalArrangement = Arrangement.spacedBy(MahinSpacing.md),
    ) {
        JalaliMonthHeader(
            monthNames = monthNames,
            visibleYear = visibleYear,
            visibleMonth = visibleMonth,
            onPreviousMonth = {
                if (visibleMonth == 1) {
                    visibleMonth = 12
                    visibleYear -= 1
                } else {
                    visibleMonth -= 1
                }
            },
            onNextMonth = {
                if (visibleMonth == 12) {
                    visibleMonth = 1
                    visibleYear += 1
                } else {
                    visibleMonth += 1
                }
            },
        )
        JalaliWeekdayHeaderRow()
        JalaliMonthGrid(
            visibleYear = visibleYear,
            visibleMonth = visibleMonth,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            converter = converter,
            dayBackgroundColor = dayBackgroundColor,
        )
        JalaliGregorianDetailLine(selectedDate = selectedDate, converter = converter)
    }
}

@Composable
private fun JalaliMonthHeader(
    monthNames: Array<String>,
    visibleYear: Int,
    visibleMonth: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier.mahinMinimumTouchTarget(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.ds_date_picker_prev_month),
            )
        }
        Text(
            text = "${monthNames[visibleMonth - 1]} ${PersianDigits.format(visibleYear)}",
            style = mahinTextStyle(MahinTypographyRole.Title),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        IconButton(
            onClick = onNextMonth,
            modifier = Modifier.mahinMinimumTouchTarget(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.ds_date_picker_next_month),
            )
        }
    }
}

@Composable
private fun JalaliWeekdayHeaderRow() {
    val weekdayLabels =
        listOf(
            stringResource(R.string.ds_weekday_sat),
            stringResource(R.string.ds_weekday_sun),
            stringResource(R.string.ds_weekday_mon),
            stringResource(R.string.ds_weekday_tue),
            stringResource(R.string.ds_weekday_wed),
            stringResource(R.string.ds_weekday_thu),
            stringResource(R.string.ds_weekday_fri),
        )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        weekdayLabels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun JalaliMonthGrid(
    visibleYear: Int,
    visibleMonth: Int,
    selectedDate: JalaliDate?,
    onDateSelected: (JalaliDate) -> Unit,
    converter: CivilDateConverter,
    dayBackgroundColor: (LocalDate) -> Color?,
) {
    val daysInMonth = JalaliCalendar.daysInMonth(visibleYear, visibleMonth)
    val firstGregorian = converter.toGregorian(JalaliDate(visibleYear, visibleMonth, 1))
    val leadingEmpty = jalaliWeekdayColumnIndex(firstGregorian.dayOfWeek)
    val cells =
        buildList {
            repeat(leadingEmpty) { add(null) }
            for (day in 1..daysInMonth) {
                add(JalaliDate(visibleYear, visibleMonth, day))
            }
        }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MahinSpacing.xxs),
    ) {
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MahinSpacing.xxs),
            ) {
                week.forEach { date ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (date == null) {
                            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f))
                        } else {
                            JalaliDayCell(
                                date = date,
                                selected = date == selectedDate,
                                onClick = { onDateSelected(date) },
                                converter = converter,
                                markerColor = dayBackgroundColor(converter.toGregorian(date)),
                            )
                        }
                    }
                }
                repeat(7 - week.size) {
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                }
            }
        }
    }
}

@Composable
private fun JalaliGregorianDetailLine(
    selectedDate: JalaliDate?,
    converter: CivilDateConverter,
) {
    if (selectedDate == null) return
    val gregorian = converter.toGregorian(selectedDate)
    val gregorianLabel =
        stringResource(
            R.string.ds_date_picker_gregorian_detail,
            PersianDigits.format(gregorian.format(DateTimeFormatter.ISO_LOCAL_DATE)),
        )
    Text(
        text = gregorianLabel,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun JalaliDayCell(
    date: JalaliDate,
    selected: Boolean,
    onClick: () -> Unit,
    converter: CivilDateConverter,
    markerColor: Color? = null,
) {
    val monthNames = stringArrayResource(R.array.ds_jalali_month_names)
    val monthName = monthNames[date.month - 1]
    val dayLabel = PersianDigits.format(date.day)
    val description =
        remember(date, monthName) {
            val g = converter.toGregorian(date)
            "${PersianDigits.format(date.day)} $monthName ${PersianDigits.format(date.year)} — " +
                PersianDigits.format(g.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }
    val backgroundColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            markerColor ?: MaterialTheme.colorScheme.surface
        }
    val contentColor =
        if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        }

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .mahinMinimumTouchTarget()
                .clip(RoundedCornerShape(MahinRadius.sm))
                .semantics {
                    role = Role.Button
                    this.selected = selected
                    contentDescription = description
                }.clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(MahinRadius.sm),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = dayLabel, style = MaterialTheme.typography.bodyMedium, color = contentColor)
        }
    }
}

/** Saturday-first week column (Iranian calendar grid). */
private fun jalaliWeekdayColumnIndex(dayOfWeek: DayOfWeek): Int =
    when (dayOfWeek) {
        DayOfWeek.SATURDAY -> 0
        DayOfWeek.SUNDAY -> 1
        DayOfWeek.MONDAY -> 2
        DayOfWeek.TUESDAY -> 3
        DayOfWeek.WEDNESDAY -> 4
        DayOfWeek.THURSDAY -> 5
        DayOfWeek.FRIDAY -> 6
        else -> 0
    }
