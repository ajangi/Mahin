package dev.mahin.android.cycle

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import dev.mahin.core.designsystem.component.MahinScreenHeader

@Composable
@Suppress("LongMethod")
internal fun LogScreenContent(
    state: LogUiState,
    actions: LogScreenActions,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("log_screen_list")
                .padding(MahinSpacing.md),
    ) {
        item {
            MahinScreenHeader(
                title = stringResource(R.string.log_title),
                subtitle = stringResource(R.string.log_subtitle),
            )
        }
        item {
            MahinJalaliDatePicker(
                selectedDate = state.selectedJalali,
                onDateSelected = actions.onDateSelected,
                converter = state.converter,
                initialVisibleMonth = state.selectedJalali,
            )
        }
        item {
            MahinPrimaryButton(
                text = stringResource(R.string.log_open_day_editor),
                onClick = actions.onOpenDayEditor,
                modifier = Modifier.testTag("log_open_day_editor"),
            )
        }
    }
}
