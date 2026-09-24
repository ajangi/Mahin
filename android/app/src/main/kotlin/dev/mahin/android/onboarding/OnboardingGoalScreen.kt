package dev.mahin.android.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import dev.mahin.core.designsystem.mahinTextStyle
import dev.mahin.core.model.ReproductiveMode

@Composable
fun OnboardingGoalScreen(
    selectedMode: ReproductiveMode,
    onModeSelected: (ReproductiveMode) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.onboarding_goal_title),
            style = mahinTextStyle(MahinTypographyRole.TitleLarge),
        )
        Spacer(modifier = Modifier.height(MahinSpacing.sm))
        Text(
            text = stringResource(R.string.onboarding_goal_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(MahinSpacing.md))
        GoalChip(
            label = stringResource(R.string.onboarding_goal_cycle),
            selected = selectedMode == ReproductiveMode.CYCLE_TRACKING,
            onClick = { onModeSelected(ReproductiveMode.CYCLE_TRACKING) },
        )
        GoalChip(
            label = stringResource(R.string.onboarding_goal_ttc),
            selected = selectedMode == ReproductiveMode.TRYING_TO_CONCEIVE,
            onClick = { onModeSelected(ReproductiveMode.TRYING_TO_CONCEIVE) },
        )
        Text(
            text = stringResource(R.string.onboarding_goal_pregnancy_deferred),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MahinSpacing.sm),
        )
        Spacer(modifier = Modifier.height(MahinSpacing.lg))
        MahinPrimaryButton(
            text = stringResource(R.string.onboarding_continue),
            onClick = onContinue,
        )
    }
}

@Composable
private fun GoalChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = MahinSpacing.xs),
    )
}
