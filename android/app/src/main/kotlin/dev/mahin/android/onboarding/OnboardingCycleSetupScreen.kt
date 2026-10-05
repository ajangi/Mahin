package dev.mahin.android.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinChoiceChip
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import dev.mahin.core.designsystem.component.MahinScreenHeader
import dev.mahin.core.designsystem.component.MahinSectionLabel
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode

@Suppress("LongParameterList")
@Composable
fun OnboardingCycleSetupScreen(
    form: OnboardingSetupFormState,
    onLastPeriodStartChange: (java.time.LocalDate) -> Unit,
    onCycleLengthChange: (Int?) -> Unit,
    onPeriodLengthChange: (Int?) -> Unit,
    onRegularityChange: (CycleRegularity) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val converter = remember { PersianCivilDateConverter }
    val startJalali = remember(form.lastPeriodStart) { converter.toJalali(form.lastPeriodStart) }
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag("onboarding_cycle_setup_screen"),
    ) {
        MahinScreenHeader(
            title =
                if (form.mode == ReproductiveMode.TRYING_TO_CONCEIVE) {
                    stringResource(R.string.onboarding_setup_ttc_title)
                } else {
                    stringResource(R.string.onboarding_setup_cycle_title)
                },
            subtitle = stringResource(R.string.onboarding_setup_body),
        )
        MahinSectionLabel(text = stringResource(R.string.onboarding_last_period_start))
        MahinJalaliDatePicker(
            selectedDate = startJalali,
            onDateSelected = { onLastPeriodStartChange(converter.toGregorian(it)) },
            converter = converter,
            initialVisibleMonth = startJalali,
        )
        Spacer(modifier = Modifier.height(MahinSpacing.md))
        OutlinedTextField(
            value = form.typicalCycleLength?.toString() ?: "",
            onValueChange = { onCycleLengthChange(it.toIntOrNull()) },
            label = { Text(stringResource(R.string.onboarding_typical_cycle_length)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = form.typicalPeriodLength?.toString() ?: "",
            onValueChange = { onPeriodLengthChange(it.toIntOrNull()) },
            label = { Text(stringResource(R.string.onboarding_typical_period_length)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        MahinSectionLabel(text = stringResource(R.string.onboarding_regularity_label))
        CycleRegularity.entries.forEach { option ->
            MahinChoiceChip(
                label = regularityLabel(option),
                selected = form.regularity == option,
                onClick = { onRegularityChange(option) },
            )
        }
        Spacer(modifier = Modifier.height(MahinSpacing.lg))
        MahinPrimaryButton(
            text = stringResource(R.string.onboarding_finish),
            onClick = onFinish,
            enabled = !form.isSaving,
        )
    }
}

@Composable
private fun regularityLabel(regularity: CycleRegularity): String =
    when (regularity) {
        CycleRegularity.REGULAR -> stringResource(R.string.regularity_regular)
        CycleRegularity.SOMEWHAT_IRREGULAR -> stringResource(R.string.regularity_somewhat)
        CycleRegularity.IRREGULAR -> stringResource(R.string.regularity_irregular)
        CycleRegularity.UNKNOWN -> stringResource(R.string.regularity_unknown)
    }
