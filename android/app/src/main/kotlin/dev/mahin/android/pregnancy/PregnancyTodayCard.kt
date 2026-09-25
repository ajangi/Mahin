package dev.mahin.android.pregnancy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.android.cycle.CycleFormatters
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.domain.pregnancy.PregnancyStatusSnapshot

@Composable
fun PregnancyTodayCard(
    status: PregnancyStatusSnapshot,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
            val ga = status.gestationalAge
            val gaLabel =
                "${PregnancyFormatters.formatInteger(ga.weeks)} هفته و " +
                    "${PregnancyFormatters.formatInteger(ga.days)} روز"
            Text(
                text = stringResource(R.string.pregnancy_gestational_age, gaLabel),
                style = mahinTextStyle(MahinTypographyRole.NumericDisplay),
            )
            Text(
                text = stringResource(pregnancyTrimesterLabelRes(status.trimester)),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text =
                    stringResource(
                        R.string.pregnancy_edd_countdown,
                        PregnancyFormatters.formatLong(status.daysUntilEdd),
                    ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text =
                    stringResource(
                        R.string.pregnancy_week_card_title,
                        PregnancyFormatters.formatInteger(status.displayWeekNumber),
                    ),
                style = mahinTextStyle(MahinTypographyRole.Label),
                modifier = Modifier.padding(top = MahinSpacing.sm),
            )
            Text(
                text = stringResource(R.string.pregnancy_week_placeholder_fetal),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text =
                    stringResource(
                        R.string.pregnancy_effective_edd,
                        CycleFormatters.formatLocalDate(status.dating.effectiveEddDate),
                    ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = MahinSpacing.xs),
            )
            Text(
                text = stringResource(R.string.pregnancy_dating_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
