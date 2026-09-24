package dev.mahin.android.cycle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.core.model.ReproductiveMode

@Composable
internal fun LogScreenContent(
    state: LogUiState,
    actions: LogScreenActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(MahinSpacing.md),
    ) {
        Text(
            text = stringResource(R.string.log_title),
            style = mahinTextStyle(MahinTypographyRole.TitleLarge),
        )
        MahinJalaliDatePicker(
            selectedDate = state.selectedJalali,
            onDateSelected = actions.onDateSelected,
            converter = state.converter,
            initialVisibleMonth = state.selectedJalali,
        )
        CycleLogFields(state = state, actions = actions)
        if (state.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE) {
            TtcLogSections(
                form =
                    TtcLogFormState(
                        bbtInput = state.bbtInput,
                        ovulationTest = state.ovulationTest,
                        cervicalMucus = state.cervicalMucus,
                        intercourseLogged = state.intercourseLogged,
                        intercourseProtected = state.intercourseProtected,
                        pregnancyTest = state.pregnancyTest,
                    ),
                callbacks = actions.ttcCallbacks,
            )
        }
        Spacer(modifier = Modifier.height(MahinSpacing.lg))
        MahinPrimaryButton(
            text = stringResource(R.string.log_save),
            onClick = actions.onSave,
            enabled = !state.saving,
        )
        if (state.saved) {
            Text(
                text = stringResource(R.string.log_saved_confirmation),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
        }
    }
}
