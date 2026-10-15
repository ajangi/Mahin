package dev.mahin.android.pregnancy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.android.cycle.CycleFormatters
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import dev.mahin.core.designsystem.component.MahinScreenHeader
import dev.mahin.domain.pregnancy.PregnancyToolsCalculator
import java.time.LocalDate

enum class PregnancyToolMode {
    LMP,
    ULTRASOUND,
    CONCEPTION,
}

data class PregnancyToolsResultState(
    val effectiveEddLabel: String,
    val gestationalAgeLabel: String,
    val displayWeek: String,
)

@Composable
fun PregnancyToolsScreenContent(
    referenceDate: LocalDate,
    modifier: Modifier = Modifier,
) {
    var mode by remember { mutableStateOf(PregnancyToolMode.LMP) }
    var selectedJalali by remember { mutableStateOf(PersianCivilDateConverter.toJalali(referenceDate.minusWeeks(8))) }
    val gregorian = PersianCivilDateConverter.toGregorian(selectedJalali)
    val result =
        remember(mode, gregorian, referenceDate) {
            val tool =
                when (mode) {
                    PregnancyToolMode.LMP -> PregnancyToolsCalculator.fromLmp(gregorian, referenceDate)
                    PregnancyToolMode.ULTRASOUND -> PregnancyToolsCalculator.fromUltrasoundEdd(gregorian, referenceDate)
                    PregnancyToolMode.CONCEPTION -> PregnancyToolsCalculator.fromConception(gregorian, referenceDate)
                }
            PregnancyToolsResultState(
                effectiveEddLabel = CycleFormatters.formatLocalDate(tool.effectiveEdd),
                gestationalAgeLabel =
                    "${PregnancyFormatters.formatInteger(tool.gestationalAge.weeks)} هفته و " +
                        "${PregnancyFormatters.formatInteger(tool.gestationalAge.days)} روز",
                displayWeek = PregnancyFormatters.formatInteger(tool.displayWeekNumber),
            )
        }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("pregnancy_tools_hub")
                .verticalScroll(rememberScrollState())
                .padding(MahinSpacing.md),
    ) {
        MahinScreenHeader(
            title = stringResource(R.string.pregnancy_tools_title),
            subtitle = stringResource(R.string.pregnancy_tools_subtitle),
        )
        Text(
            text = stringResource(R.string.pregnancy_tools_no_persist_notice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = MahinSpacing.sm),
        )
        PregnancyToolMode.entries.forEach { option ->
            FilterChip(
                selected = mode == option,
                onClick = { mode = option },
                label = { Text(stringResource(toolModeLabel(option))) },
                modifier = Modifier.fillMaxWidth().padding(vertical = MahinSpacing.xs),
            )
        }
        MahinJalaliDatePicker(
            selectedDate = selectedJalali,
            onDateSelected = { selectedJalali = it },
            converter = PersianCivilDateConverter,
            initialVisibleMonth = selectedJalali,
        )
        Text(
            text = stringResource(R.string.pregnancy_effective_edd, result.effectiveEddLabel),
            modifier = Modifier.padding(top = MahinSpacing.md),
        )
        Text(text = stringResource(R.string.pregnancy_gestational_age, result.gestationalAgeLabel))
        Text(text = stringResource(R.string.pregnancy_tools_display_week, result.displayWeek))
        Text(
            text = stringResource(R.string.pregnancy_dating_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MahinSpacing.md),
        )
    }
}

private fun toolModeLabel(mode: PregnancyToolMode): Int =
    when (mode) {
        PregnancyToolMode.LMP -> R.string.pregnancy_tools_mode_lmp
        PregnancyToolMode.ULTRASOUND -> R.string.pregnancy_tools_mode_ultrasound
        PregnancyToolMode.CONCEPTION -> R.string.pregnancy_tools_mode_conception
    }
