package dev.mahin.android.cycle

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.android.pregnancy.PregnancyLogCallbacks
import dev.mahin.android.pregnancy.PregnancyLogFormState
import dev.mahin.android.pregnancy.PregnancyLogSections
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import dev.mahin.core.designsystem.component.MahinScreenHeader
import dev.mahin.core.model.ReproductiveMode

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
        if (state.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE) {
            item {
                TtcLogSections(
                    form =
                        TtcLogFormState(
                            bbtInput = state.bbtInput,
                            bbtError = state.bbtError,
                            intercourseLoggingEnabled = state.intercourseLoggingEnabled,
                            ovulationTest = state.ovulationTest,
                            cervicalMucus = state.cervicalMucus,
                            intercourseLogged = state.intercourseLogged,
                            intercourseProtected = state.intercourseProtected,
                            pregnancyTest = state.pregnancyTest,
                        ),
                    callbacks = actions.ttcCallbacks,
                )
            }
        }
        if (state.reproductiveMode == ReproductiveMode.PREGNANT) {
            item {
                PregnancyLogSections(
                    form =
                        PregnancyLogFormState(
                            pregnancySymptomTags = state.pregnancySymptomTags,
                            weightInput = state.pregnancyWeightInput,
                            weightError = state.pregnancyWeightError,
                            bpSystolicInput = state.pregnancyBpSystolicInput,
                            bpDiastolicInput = state.pregnancyBpDiastolicInput,
                            bpError = state.pregnancyBpError,
                            availableSymptoms = state.pregnancyAvailableSymptoms,
                        ),
                    callbacks =
                        PregnancyLogCallbacks(
                            onSymptomToggle = actions.onTogglePregnancySymptom,
                            onWeightChange = actions.onPregnancyWeightChange,
                            onBpSystolicChange = actions.onPregnancyBpSystolicChange,
                            onBpDiastolicChange = actions.onPregnancyBpDiastolicChange,
                        ),
                )
            }
        }
    }
}
