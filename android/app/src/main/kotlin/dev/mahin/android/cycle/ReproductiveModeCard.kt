package dev.mahin.android.cycle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.core.model.ReproductiveMode

@Composable
fun ReproductiveModeCard(
    currentMode: ReproductiveMode,
    onModeSelected: (ReproductiveMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(MahinSpacing.md)) {
            Text(
                text = stringResource(R.string.mode_settings_title),
                style = mahinTextStyle(MahinTypographyRole.Label),
            )
            Text(
                text = stringResource(R.string.mode_settings_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FilterChip(
                selected = currentMode == ReproductiveMode.CYCLE_TRACKING,
                onClick = { onModeSelected(ReproductiveMode.CYCLE_TRACKING) },
                label = { Text(stringResource(R.string.onboarding_goal_cycle)) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = MahinSpacing.xs),
            )
            FilterChip(
                selected = currentMode == ReproductiveMode.TRYING_TO_CONCEIVE,
                onClick = { onModeSelected(ReproductiveMode.TRYING_TO_CONCEIVE) },
                label = { Text(stringResource(R.string.onboarding_goal_ttc)) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = MahinSpacing.xs),
            )
        }
    }
}
