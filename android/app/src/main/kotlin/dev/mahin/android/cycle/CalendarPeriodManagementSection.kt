package dev.mahin.android.cycle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.datetime.PersianDigits
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinSectionLabel
import java.time.LocalDate

@Composable
@Suppress("LongParameterList", "LongMethod")
internal fun CalendarPeriodManagementSection(
    periods: List<PeriodRecordEntity>,
    editingRecordId: String?,
    editStartJalali: JalaliDate?,
    editEndJalali: JalaliDate?,
    onBeginEdit: (String) -> Unit,
    onEditStartSelected: (JalaliDate) -> Unit,
    onEditEndSelected: (JalaliDate) -> Unit,
    onClearEndDate: (String) -> Unit,
    onSaveEdit: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onMergeWithNext: (String) -> Unit,
    onSplit: (String) -> Unit,
    splitRecordId: String? = null,
    splitAfterJalali: JalaliDate? = null,
    onSplitAfterSelected: (JalaliDate) -> Unit = {},
    onConfirmSplit: () -> Unit = {},
    onCancelSplit: () -> Unit = {},
    feedbackMessageKey: String? = null,
    modifier: Modifier = Modifier,
) {
    val converter = PersianCivilDateConverter
    Column(modifier = modifier.fillMaxWidth().testTag("calendar_period_management")) {
        MahinSectionLabel(text = stringResource(R.string.calendar_period_management_title))
        if (feedbackMessageKey != null) {
            Text(
                text = periodFeedbackText(feedbackMessageKey),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("calendar_period_feedback"),
            )
        }
        if (periods.isEmpty()) {
            Text(
                text = stringResource(R.string.calendar_period_management_empty),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = MahinSpacing.sm),
            )
        }
        periods.forEachIndexed { index, record ->
            val endLabel =
                record.endDate?.let { formatGregorian(it, converter) }
                    ?: stringResource(R.string.calendar_period_ongoing)
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = MahinSpacing.xs)
                        .testTag("calendar_period_row_${record.id}"),
            ) {
                Text(
                    text =
                        stringResource(
                            R.string.calendar_period_range_summary,
                            formatGregorian(record.startDate, converter),
                            endLabel,
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
                    TextButton(
                        onClick = { onBeginEdit(record.id) },
                        modifier = Modifier.testTag("calendar_period_edit_${record.id}"),
                    ) {
                        Text(stringResource(R.string.calendar_period_edit_dates))
                    }
                    if (index < periods.lastIndex) {
                        TextButton(
                            onClick = { onMergeWithNext(record.id) },
                            modifier = Modifier.testTag("calendar_period_merge_${record.id}"),
                        ) {
                            Text(stringResource(R.string.calendar_period_merge_next))
                        }
                    }
                    val periodEnd = record.endDate
                    if (periodEnd != null && periodEnd.isAfter(record.startDate)) {
                        TextButton(
                            onClick = { onSplit(record.id) },
                            modifier = Modifier.testTag("calendar_period_split_${record.id}"),
                        ) {
                            Text(stringResource(R.string.calendar_period_split))
                        }
                    }
                }
            }
        }
        if (splitRecordId != null && splitAfterJalali != null) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = MahinSpacing.md)
                        .testTag("calendar_period_split_editor"),
            ) {
                Text(text = stringResource(R.string.calendar_period_split_after_label))
                dev.mahin.core.designsystem.component.MahinJalaliDatePicker(
                    selectedDate = splitAfterJalali,
                    onDateSelected = onSplitAfterSelected,
                    converter = converter,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
                    TextButton(
                        onClick = onConfirmSplit,
                        modifier = Modifier.testTag("calendar_period_split_confirm"),
                    ) {
                        Text(stringResource(R.string.calendar_period_split))
                    }
                    TextButton(
                        onClick = onCancelSplit,
                        modifier = Modifier.testTag("calendar_period_split_cancel"),
                    ) {
                        Text(stringResource(R.string.calendar_period_cancel))
                    }
                }
            }
        }
        if (editingRecordId != null && editStartJalali != null) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = MahinSpacing.md)
                        .testTag("calendar_period_editor"),
            ) {
                Text(text = stringResource(R.string.calendar_period_start_label))
                dev.mahin.core.designsystem.component.MahinJalaliDatePicker(
                    selectedDate = editStartJalali,
                    onDateSelected = onEditStartSelected,
                    converter = converter,
                )
                Text(
                    text = stringResource(R.string.calendar_period_end_label),
                    modifier = Modifier.padding(top = MahinSpacing.sm),
                )
                if (editEndJalali != null) {
                    dev.mahin.core.designsystem.component.MahinJalaliDatePicker(
                        selectedDate = editEndJalali,
                        onDateSelected = onEditEndSelected,
                        converter = converter,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.calendar_period_ongoing),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(MahinSpacing.sm)) {
                    TextButton(
                        onClick = { onClearEndDate(editingRecordId) },
                        modifier = Modifier.testTag("calendar_period_clear_end"),
                    ) {
                        Text(stringResource(R.string.calendar_period_set_ongoing))
                    }
                    TextButton(
                        onClick = { onSaveEdit(editingRecordId) },
                        modifier = Modifier.testTag("calendar_period_save"),
                    ) {
                        Text(stringResource(R.string.calendar_period_save))
                    }
                    TextButton(
                        onClick = onCancelEdit,
                        modifier = Modifier.testTag("calendar_period_cancel"),
                    ) {
                        Text(stringResource(R.string.calendar_period_cancel))
                    }
                }
            }
        }
    }
}

@Composable
private fun periodFeedbackText(key: String): String =
    when (key) {
        "overlap" -> stringResource(R.string.calendar_period_error_overlap)
        "merge_not_adjacent" -> stringResource(R.string.calendar_period_error_merge)
        "invalid_split" -> stringResource(R.string.calendar_period_error_split)
        else -> stringResource(R.string.calendar_period_error_overlap)
    }

private fun formatGregorian(
    date: LocalDate,
    converter: PersianCivilDateConverter,
): String {
    val jalali = converter.toJalali(date)
    return "${PersianDigits.format(jalali.year)}/${PersianDigits.format(jalali.month)}/" +
        PersianDigits.format(jalali.day)
}
