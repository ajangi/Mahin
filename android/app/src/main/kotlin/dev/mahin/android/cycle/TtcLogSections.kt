package dev.mahin.android.cycle

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import dev.mahin.android.R
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
    TtcBbtField(form.bbtInput, callbacks.onBbtChange)
    TtcOpkSection(form.ovulationTest, callbacks.onOvulationTestSelected)
    TtcMucusSection(form.cervicalMucus, callbacks.onCervicalMucusSelected)
    TtcIntercourseSection(
        intercourseLogged = form.intercourseLogged,
        intercourseProtected = form.intercourseProtected,
        onIntercourseToggle = callbacks.onIntercourseToggle,
        onIntercourseProtectedSelected = callbacks.onIntercourseProtectedSelected,
    )
    TtcPregnancyTestSection(form.pregnancyTest, callbacks.onPregnancyTestSelected)
}

@Composable
private fun TtcBbtField(
    bbtInput: String,
    onBbtChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = bbtInput,
        onValueChange = onBbtChange,
        label = { Text(stringResource(R.string.log_bbt_label)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
private fun TtcIntercourseSection(
    intercourseLogged: Boolean,
    intercourseProtected: Boolean?,
    onIntercourseToggle: () -> Unit,
    onIntercourseProtectedSelected: (Boolean?) -> Unit,
) {
    Text(
        text = stringResource(R.string.log_intercourse_section),
        style = mahinTextStyle(MahinTypographyRole.Label),
        modifier = Modifier.padding(top = MahinSpacing.sm),
    )
    FilterChip(
        selected = intercourseLogged,
        onClick = onIntercourseToggle,
        label = { Text(stringResource(R.string.log_intercourse_toggle)) },
    )
    if (intercourseLogged) {
        FilterChip(
            selected = intercourseProtected == true,
            onClick = { onIntercourseProtectedSelected(true) },
            label = { Text(stringResource(R.string.ttc_intercourse_protected)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = MahinSpacing.xxs),
        )
        FilterChip(
            selected = intercourseProtected == false,
            onClick = { onIntercourseProtectedSelected(false) },
            label = { Text(stringResource(R.string.ttc_intercourse_unprotected)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = MahinSpacing.xxs),
        )
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
    }

@Composable
private fun pregnancyTestLabel(result: PregnancyTestResult): String =
    when (result) {
        PregnancyTestResult.NEGATIVE -> stringResource(R.string.pregnancy_test_negative)
        PregnancyTestResult.POSITIVE -> stringResource(R.string.pregnancy_test_positive)
        PregnancyTestResult.UNCLEAR -> stringResource(R.string.pregnancy_test_unclear)
    }
