package dev.mahin.android.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.datetime.PregnancyClinicalEddInput
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinChoiceChip
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import dev.mahin.core.designsystem.component.MahinScreenHeader
import dev.mahin.core.designsystem.component.MahinSectionLabel

@Suppress("LongParameterList")
@Composable
fun OnboardingPregnancySetupScreen(
    form: OnboardingPregnancyFormState,
    onLmpChange: (JalaliDate) -> Unit,
    onIncludeClinicalToggle: () -> Unit,
    onClinicalEddChange: (JalaliDate) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag("onboarding_pregnancy_setup_screen"),
    ) {
        MahinScreenHeader(
            title = stringResource(R.string.onboarding_pregnancy_setup_title),
            subtitle = stringResource(R.string.onboarding_pregnancy_setup_body),
        )
        MahinSectionLabel(text = stringResource(R.string.pregnancy_lmp_label))
        MahinJalaliDatePicker(
            selectedDate = form.lmpJalali,
            onDateSelected = onLmpChange,
            converter = PersianCivilDateConverter,
            initialVisibleMonth = form.lmpJalali,
        )
        MahinChoiceChip(
            label = stringResource(R.string.pregnancy_clinical_edd_label),
            selected = form.includeClinicalEdd,
            onClick = onIncludeClinicalToggle,
        )
        if (form.includeClinicalEdd) {
            val clinical =
                form.clinicalEddJalali ?: PregnancyClinicalEddInput.defaultClinicalEddJalali()
            MahinJalaliDatePicker(
                selectedDate = clinical,
                onDateSelected = onClinicalEddChange,
                converter = PersianCivilDateConverter,
                initialVisibleMonth = clinical,
            )
        }
        Text(
            text = stringResource(R.string.pregnancy_dating_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MahinSpacing.sm),
        )
        Spacer(modifier = Modifier.height(MahinSpacing.lg))
        MahinPrimaryButton(
            text = stringResource(R.string.onboarding_finish),
            onClick = onFinish,
            enabled = !form.isSaving,
        )
    }
}
