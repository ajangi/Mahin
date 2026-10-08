package dev.mahin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.launch

@Suppress("LongParameterList")
@Composable
fun MahinJalaliDatePicker(
    selectedDate: JalaliDate?,
    onDateSelected: (JalaliDate) -> Unit,
    modifier: Modifier = Modifier,
    converter: CivilDateConverter = PersianCivilDateConverter,
    initialVisibleMonth: JalaliDate = selectedDate ?: converter.toJalali(LocalDate.now()),
    visibleMonth: JalaliDate? = null,
    onVisibleMonthChanged: ((JalaliDate) -> Unit)? = null,
    dayBackgroundColor: (LocalDate) -> Color? = { null },
    dayDecoration: (LocalDate) -> MahinCalendarDayDecoration? = { null },
    dayCellModifier: @Composable (LocalDate) -> Modifier = { Modifier },
    headerTrailing: @Composable (() -> Unit)? = null,
) {
    val monthNames = stringArrayResource(R.array.ds_jalali_month_names)
    val controlledMonth = visibleMonth ?: initialVisibleMonth
    val pagerState =
        rememberPagerState(
            initialPage = jalaliMonthPageIndex(controlledMonth),
            pageCount = { JALALI_MONTH_PAGE_COUNT },
        )
    LaunchedEffect(visibleMonth) {
        visibleMonth?.let { target ->
            val page = jalaliMonthPageIndex(target)
            if (pagerState.currentPage != page) {
                pagerState.scrollToPage(page)
            }
        }
    }
    LaunchedEffect(pagerState, onVisibleMonthChanged) {
        if (onVisibleMonthChanged == null) return@LaunchedEffect
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onVisibleMonthChanged(jalaliMonthFromPageIndex(page))
        }
    }
    val currentMonth = jalaliMonthFromPageIndex(pagerState.currentPage)
    val scope = rememberCoroutineScope()
    Column(
        modifier = modifier.fillMaxWidth().padding(MahinSpacing.md),
        verticalArrangement = Arrangement.spacedBy(MahinSpacing.md),
    ) {
        JalaliMonthHeader(
            monthNames = monthNames,
            visibleYear = currentMonth.year,
            visibleMonth = currentMonth.month,
            trailing = headerTrailing,
            onPreviousMonth = {
                val target = (pagerState.currentPage - 1).coerceAtLeast(0)
                scope.launch { pagerState.animateScrollToPage(target) }
            },
            onNextMonth = {
                val target = (pagerState.currentPage + 1).coerceAtMost(JALALI_MONTH_PAGE_COUNT - 1)
                scope.launch { pagerState.animateScrollToPage(target) }
            },
        )
        JalaliWeekdayHeaderRow()
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
            val month = jalaliMonthFromPageIndex(page)
            JalaliMonthGrid(
                visibleYear = month.year,
                visibleMonth = month.month,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                converter = converter,
                dayBackgroundColor = dayBackgroundColor,
                dayDecoration = dayDecoration,
                dayCellModifier = dayCellModifier,
            )
        }
        JalaliGregorianDetailLine(selectedDate = selectedDate, converter = converter)
    }
}

private const val JALALI_MONTH_BASE_YEAR = 1370
private const val JALALI_MONTH_PAGE_COUNT = 12 * 80

private fun jalaliMonthPageIndex(date: JalaliDate): Int = (date.year - JALALI_MONTH_BASE_YEAR) * 12 + (date.month - 1)

private fun jalaliMonthFromPageIndex(page: Int): JalaliDate {
    val year = JALALI_MONTH_BASE_YEAR + page / 12
    val month = (page % 12) + 1
    return JalaliDate(year, month, 1)
}

@Suppress("LongParameterList")
@Composable
private fun JalaliMonthHeader(
    monthNames: Array<String>,
    visibleYear: Int,
    visibleMonth: Int,
    trailing: @Composable (() -> Unit)?,
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            trailing?.invoke()
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
    dayDecoration: (LocalDate) -> MahinCalendarDayDecoration?,
    dayCellModifier: @Composable (LocalDate) -> Modifier = { Modifier },
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
                            val gregorian = converter.toGregorian(date)
                            JalaliDayCell(
                                date = date,
                                selected = date == selectedDate,
                                onClick = { onDateSelected(date) },
                                converter = converter,
                                markerColor = dayBackgroundColor(gregorian),
                                decoration = dayDecoration(gregorian),
                                modifier = dayCellModifier(gregorian),
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

@Suppress("LongParameterList", "LongMethod")
@Composable
private fun JalaliDayCell(
    date: JalaliDate,
    selected: Boolean,
    onClick: () -> Unit,
    converter: CivilDateConverter,
    markerColor: Color? = null,
    decoration: MahinCalendarDayDecoration? = null,
    modifier: Modifier = Modifier,
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
    val fill =
        when {
            selected -> MaterialTheme.colorScheme.primary
            decoration?.fillColor != null -> decoration.fillColor
            markerColor != null -> markerColor
            else -> MaterialTheme.colorScheme.surface
        }
    val contentColor =
        if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    val shape = RoundedCornerShape(MahinRadius.sm)
    val borderModifier =
        run {
            var mod: Modifier = Modifier
            if (!selected && decoration?.predictedPeriodOutline == true) {
                mod =
                    mod.border(
                        width = 1.dp,
                        color = MahinCalendarMarkerTints.periodPredictedBorder(),
                        shape = shape,
                    )
            }
            if (!selected && decoration?.isToday == true) {
                mod =
                    mod.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = shape,
                    )
            }
            mod
        }

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .mahinMinimumTouchTarget()
                .clip(shape)
                .then(borderModifier)
                .semantics {
                    role = Role.Button
                    this.selected = selected
                    contentDescription = description
                }.clickable(onClick = onClick),
        color = fill,
        shape = shape,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = dayLabel, style = MaterialTheme.typography.bodyMedium, color = contentColor)
            if (decoration?.hasLogEntries == true) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                )
            }
            if (decoration?.estimatedOvulation == true) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(3.dp)
                            .size(7.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(MahinCalendarMarkerTints.estimatedOvulation()),
                )
            }
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
