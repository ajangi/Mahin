package dev.mahin.android.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.datetime.PregnancyClinicalEddInput
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import dev.mahin.core.designsystem.component.MahinPrimaryButton

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
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.onboarding_pregnancy_setup_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.onboarding_pregnancy_setup_body),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = MahinSpacing.sm),
        )
        Text(text = stringResource(R.string.pregnancy_lmp_label))
        MahinJalaliDatePicker(
            selectedDate = form.lmpJalali,
            onDateSelected = onLmpChange,
            converter = PersianCivilDateConverter,
            initialVisibleMonth = form.lmpJalali,
        )
        androidx.compose.material3.FilterChip(
            selected = form.includeClinicalEdd,
            onClick = onIncludeClinicalToggle,
            label = { Text(stringResource(R.string.pregnancy_clinical_edd_label)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = MahinSpacing.sm),
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
        )
        MahinPrimaryButton(
            text = stringResource(R.string.onboarding_finish),
            onClick = onFinish,
            enabled = !form.isSaving,
            modifier = Modifier.padding(top = MahinSpacing.lg),
        )
    }
}
