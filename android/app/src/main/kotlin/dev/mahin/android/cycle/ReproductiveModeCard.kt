package dev.mahin.android.cycle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinChoiceChip
import dev.mahin.core.designsystem.component.MahinSurfaceCard
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.core.model.ReproductiveMode

@Composable
fun ReproductiveModeCard(
    currentMode: ReproductiveMode,
    hasActivePregnancy: Boolean,
    onModeSelected: (ReproductiveMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    MahinSurfaceCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(R.string.mode_settings_title),
                style = mahinTextStyle(MahinTypographyRole.Label),
            )
            Text(
                text = stringResource(R.string.mode_settings_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            MahinChoiceChip(
                label = stringResource(R.string.onboarding_goal_cycle),
                selected = currentMode == ReproductiveMode.CYCLE_TRACKING,
                onClick = { onModeSelected(ReproductiveMode.CYCLE_TRACKING) },
            )
            MahinChoiceChip(
                label = stringResource(R.string.onboarding_goal_ttc),
                selected = currentMode == ReproductiveMode.TRYING_TO_CONCEIVE,
                onClick = { onModeSelected(ReproductiveMode.TRYING_TO_CONCEIVE) },
            )
            MahinChoiceChip(
                label = stringResource(R.string.onboarding_goal_pregnancy),
                selected = currentMode == ReproductiveMode.PREGNANT || hasActivePregnancy,
                onClick = { onModeSelected(ReproductiveMode.PREGNANT) },
            )
        }
    }
}
