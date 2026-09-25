package dev.mahin.android.pregnancy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.datetime.PregnancyClinicalEddInput
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import java.time.LocalDate

@Composable
fun PregnancyStartSheetContent(
    onConfirm: (lmpDate: LocalDate, clinicalEdd: LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var lmpJalali by remember { mutableStateOf(PersianCivilDateConverter.toJalali(LocalDate.now().minusWeeks(8))) }
    var clinicalJalali by remember { mutableStateOf<JalaliDate?>(null) }
    var includeClinical by remember { mutableStateOf(false) }
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
            selectedDate = lmpJalali,
            onDateSelected = { lmpJalali = it },
            converter = PersianCivilDateConverter,
            initialVisibleMonth = lmpJalali,
        )
        FilterChip(
            selected = includeClinical,
            onClick = {
                if (!includeClinical) {
                    clinicalJalali = PregnancyClinicalEddInput.defaultClinicalEddJalali()
                }
                includeClinical = !includeClinical
            },
            label = { Text(stringResource(R.string.pregnancy_clinical_edd_label)) },
            modifier = Modifier.fillMaxWidth(),
        )
        if (includeClinical) {
            val clinical =
                clinicalJalali ?: PregnancyClinicalEddInput.defaultClinicalEddJalali()
            MahinJalaliDatePicker(
                selectedDate = clinical,
                onDateSelected = { clinicalJalali = it },
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
        MahinPrimaryButton(
            text = stringResource(R.string.pregnancy_start_confirm),
            onClick = {
                val lmp = PersianCivilDateConverter.toGregorian(lmpJalali)
                val clinical =
                    PregnancyClinicalEddInput.resolveClinicalEddGregorian(
                        includeClinical = includeClinical,
                        selectedClinicalJalali = clinicalJalali,
                    )
                onConfirm(lmp, clinical)
            },
            modifier =
                Modifier
                    .padding(top = MahinSpacing.md)
                    .testTag("pregnancy_start_confirm"),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyStartSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (lmpDate: LocalDate, clinicalEdd: LocalDate?) -> Unit,
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        PregnancyStartSheetContent(
            onConfirm = onConfirm,
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .testTag("pregnancy_start_sheet_scroll")
                    .padding(MahinSpacing.md),
        )
    }
}
