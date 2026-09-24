package dev.mahin.android.cycle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import dev.mahin.android.R
import dev.mahin.android.ttc.TtcIntercourseSectionCallbacks
import dev.mahin.android.ttc.TtcIntercourseSectionState
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PregnancyTestResult

@Composable
fun TtcLogSections(
    form: TtcLogFormState,
    callbacks: TtcLogFormCallbacks,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.log_ttc_section),
        style = mahinTextStyle(MahinTypographyRole.Label),
        modifier = modifier.padding(top = MahinSpacing.md),
    )
    Text(
        text = stringResource(R.string.log_ttc_privacy_note),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    TtcBbtField(form.bbtInput, form.bbtError, callbacks.onBbtChange)
    TtcOpkSection(form.ovulationTest, callbacks.onOvulationTestSelected)
    TtcMucusSection(form.cervicalMucus, callbacks.onCervicalMucusSelected)
    TtcIntercourseOptInSection(
        state =
            TtcIntercourseSectionState(
                enabled = form.intercourseLoggingEnabled,
                intercourseLogged = form.intercourseLogged,
                intercourseProtected = form.intercourseProtected,
            ),
        callbacks =
            TtcIntercourseSectionCallbacks(
                onOptInChanged = callbacks.onIntercourseOptInChanged,
                onIntercourseToggle = callbacks.onIntercourseToggle,
                onIntercourseProtectedSelected = callbacks.onIntercourseProtectedSelected,
            ),
    )
    TtcPregnancyTestSection(form.pregnancyTest, callbacks.onPregnancyTestSelected)
}

@Composable
private fun TtcBbtField(
    bbtInput: String,
    bbtError: BbtFieldError?,
    onBbtChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = bbtInput,
        onValueChange = onBbtChange,
        label = { Text(stringResource(R.string.log_bbt_label)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = bbtError != null,
        supportingText = {
            when (bbtError) {
                BbtFieldError.UNPARSEABLE -> Text(stringResource(R.string.log_bbt_error_unparseable))
                BbtFieldError.OUT_OF_RANGE -> Text(stringResource(R.string.log_bbt_error_range))
                null -> Unit
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun TtcOpkSection(
    selected: OvulationTestResult?,
    onSelected: (OvulationTestResult) -> Unit,
) {
    Text(
        text = stringResource(R.string.log_opk_section),
        style = mahinTextStyle(MahinTypographyRole.Label),
        modifier = Modifier.padding(top = MahinSpacing.sm),
    )
    OvulationTestResult.entries.forEach { result ->
        FilterChip(
            selected = selected == result,
            onClick = { onSelected(result) },
            label = { Text(opkLabel(result)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = MahinSpacing.xxs),
        )
    }
}

@Composable
private fun TtcMucusSection(
    selected: CervicalMucusType?,
    onSelected: (CervicalMucusType) -> Unit,
) {
    Text(
        text = stringResource(R.string.log_mucus_section),
        style = mahinTextStyle(MahinTypographyRole.Label),
        modifier = Modifier.padding(top = MahinSpacing.sm),
    )
    CervicalMucusType.entries.forEach { type ->
        FilterChip(
            selected = selected == type,
            onClick = { onSelected(type) },
            label = { Text(mucusLabel(type)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = MahinSpacing.xxs),
        )
    }
}

@Composable
private fun TtcIntercourseOptInSection(
    state: TtcIntercourseSectionState,
    callbacks: TtcIntercourseSectionCallbacks,
) {
    Text(
        text = stringResource(R.string.log_intercourse_section),
        style = mahinTextStyle(MahinTypographyRole.Label),
        modifier = Modifier.padding(top = MahinSpacing.sm),
    )
    val optInLabel = stringResource(R.string.log_intercourse_opt_in_label)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .toggleable(
                    value = state.enabled,
                    onValueChange = callbacks.onOptInChanged,
                    role = Role.Switch,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = optInLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).padding(end = MahinSpacing.sm),
        )
        Switch(
            checked = state.enabled,
            onCheckedChange = null,
        )
    }
    if (state.enabled) {
        FilterChip(
            selected = state.intercourseLogged,
            onClick = callbacks.onIntercourseToggle,
            label = { Text(stringResource(R.string.log_intercourse_toggle)) },
        )
        if (state.intercourseLogged) {
            FilterChip(
                selected = state.intercourseProtected == true,
                onClick = { callbacks.onIntercourseProtectedSelected(true) },
                label = { Text(stringResource(R.string.ttc_intercourse_protected)) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = MahinSpacing.xxs),
            )
            FilterChip(
                selected = state.intercourseProtected == false,
                onClick = { callbacks.onIntercourseProtectedSelected(false) },
                label = { Text(stringResource(R.string.ttc_intercourse_unprotected)) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = MahinSpacing.xxs),
            )
        }
    }
}

@Composable
private fun TtcPregnancyTestSection(
    selected: PregnancyTestResult?,
    onSelected: (PregnancyTestResult) -> Unit,
) {
    Text(
        text = stringResource(R.string.log_pregnancy_test_section),
        style = mahinTextStyle(MahinTypographyRole.Label),
        modifier = Modifier.padding(top = MahinSpacing.sm),
    )
    PregnancyTestResult.entries.forEach { result ->
        FilterChip(
            selected = selected == result,
            onClick = { onSelected(result) },
            label = { Text(pregnancyTestLabel(result)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = MahinSpacing.xxs),
        )
    }
}

@Composable
private fun opkLabel(result: OvulationTestResult): String =
    when (result) {
        OvulationTestResult.NEGATIVE -> stringResource(R.string.opk_negative)
        OvulationTestResult.POSITIVE -> stringResource(R.string.opk_positive)
        OvulationTestResult.PEAK -> stringResource(R.string.opk_peak)
        OvulationTestResult.UNCLEAR -> stringResource(R.string.opk_unclear)
    }

@Composable
private fun mucusLabel(type: CervicalMucusType): String =
    when (type) {
        CervicalMucusType.DRY -> stringResource(R.string.mucus_dry)
        CervicalMucusType.STICKY -> stringResource(R.string.mucus_sticky)
        CervicalMucusType.CREAMY -> stringResource(R.string.mucus_creamy)
        CervicalMucusType.WATERY -> stringResource(R.string.mucus_watery)
        CervicalMucusType.EGG_WHITE -> stringResource(R.string.mucus_egg_white)
        CervicalMucusType.OTHER -> stringResource(R.string.mucus_other)
    }

@Composable
private fun pregnancyTestLabel(result: PregnancyTestResult): String =
    when (result) {
        PregnancyTestResult.NEGATIVE -> stringResource(R.string.pregnancy_test_negative)
        PregnancyTestResult.POSITIVE -> stringResource(R.string.pregnancy_test_positive)
        PregnancyTestResult.UNCLEAR -> stringResource(R.string.pregnancy_test_unclear)
    }
