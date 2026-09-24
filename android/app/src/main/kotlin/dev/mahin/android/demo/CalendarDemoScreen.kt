package dev.mahin.android.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.datetime.PersianDigits
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import dev.mahin.core.designsystem.mahinTextStyle
import java.time.LocalDate

@Composable
fun CalendarDemoScreen(modifier: Modifier = Modifier) {
    val converter = remember { PersianCivilDateConverter }
    var selected by remember {
        mutableStateOf(converter.toJalali(LocalDate.of(2024, 3, 20)))
    }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(MahinSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.calendar_demo_title),
            style = mahinTextStyle(MahinTypographyRole.TitleLarge),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.calendar_demo_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = MahinSpacing.md),
        )
        MahinJalaliDatePicker(
            selectedDate = selected,
            onDateSelected = { selected = it },
            converter = converter,
            initialVisibleMonth = selected,
        )
        Text(
            text =
                stringResource(
                    R.string.calendar_demo_selection,
                    PersianDigits.format(selected.year),
                    PersianDigits.format(selected.month),
                    PersianDigits.format(selected.day),
                ),
            style = mahinTextStyle(MahinTypographyRole.Label),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = MahinSpacing.md),
        )
    }
}
