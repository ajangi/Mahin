package dev.mahin.android.pregnancy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.android.cycle.WeightBpFieldError
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.mahinTextStyle

data class PregnancyLogFormState(
    val pregnancySymptomTags: Set<String> = emptySet(),
    val weightInput: String = "",
    val weightError: WeightBpFieldError? = null,
    val bpSystolicInput: String = "",
    val bpDiastolicInput: String = "",
    val bpError: WeightBpFieldError? = null,
    val availableSymptoms: List<String> =
        listOf("تهوع", "خستگی", "سردرد", "درد کمر", "ورم"),
)

data class PregnancyLogCallbacks(
    val onSymptomToggle: (String) -> Unit,
    val onWeightChange: (String) -> Unit,
    val onBpSystolicChange: (String) -> Unit,
    val onBpDiastolicChange: (String) -> Unit,
)

@Composable
fun PregnancyLogSections(
    form: PregnancyLogFormState,
    callbacks: PregnancyLogCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.log_pregnancy_section),
            style = mahinTextStyle(MahinTypographyRole.Label),
            modifier = Modifier.padding(top = MahinSpacing.md),
        )
        Text(
            text = stringResource(R.string.log_symptoms_section),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        form.availableSymptoms.forEach { tag ->
            FilterChip(
                selected = form.pregnancySymptomTags.contains(tag),
                onClick = { callbacks.onSymptomToggle(tag) },
                label = { Text(tag) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = MahinSpacing.xxs),
            )
        }
        OutlinedTextField(
            value = form.weightInput,
            onValueChange = callbacks.onWeightChange,
            label = { Text(stringResource(R.string.log_pregnancy_weight_label)) },
            isError = form.weightError != null,
            supportingText = {
                form.weightError?.let { error ->
                    Text(weightErrorMessage(error))
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = MahinSpacing.sm),
        )
        OutlinedTextField(
            value = form.bpSystolicInput,
            onValueChange = callbacks.onBpSystolicChange,
            label = { Text(stringResource(R.string.log_pregnancy_bp_systolic)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = MahinSpacing.sm),
        )
        OutlinedTextField(
            value = form.bpDiastolicInput,
            onValueChange = callbacks.onBpDiastolicChange,
            label = { Text(stringResource(R.string.log_pregnancy_bp_diastolic)) },
            isError = form.bpError != null,
            supportingText = {
                form.bpError?.let { error ->
                    Text(bpErrorMessage(error))
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = MahinSpacing.xs),
        )
    }
}

@Composable
private fun weightErrorMessage(error: WeightBpFieldError): String =
    when (error) {
        WeightBpFieldError.UNPARSEABLE -> stringResource(R.string.log_pregnancy_weight_error_unparseable)
        WeightBpFieldError.OUT_OF_RANGE -> stringResource(R.string.log_pregnancy_weight_error_range)
        WeightBpFieldError.BP_ORDER -> stringResource(R.string.log_pregnancy_bp_error_order)
    }

@Composable
private fun bpErrorMessage(error: WeightBpFieldError): String =
    when (error) {
        WeightBpFieldError.UNPARSEABLE -> stringResource(R.string.log_pregnancy_bp_error_unparseable)
        WeightBpFieldError.OUT_OF_RANGE -> stringResource(R.string.log_pregnancy_bp_error_range)
        WeightBpFieldError.BP_ORDER -> stringResource(R.string.log_pregnancy_bp_error_order)
    }
