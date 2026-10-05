package dev.mahin.android.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinChoiceChip
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import dev.mahin.core.designsystem.component.MahinScreenHeader
import dev.mahin.core.model.ReproductiveMode

@Composable
fun OnboardingGoalScreen(
    selectedMode: ReproductiveMode,
    onModeSelected: (ReproductiveMode) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag("onboarding_goal_screen"),
    ) {
        MahinScreenHeader(
            title = stringResource(R.string.onboarding_goal_title),
            subtitle = stringResource(R.string.onboarding_goal_body),
        )
        Spacer(modifier = Modifier.height(MahinSpacing.md))
        MahinChoiceChip(
            label = stringResource(R.string.onboarding_goal_cycle),
            selected = selectedMode == ReproductiveMode.CYCLE_TRACKING,
            onClick = { onModeSelected(ReproductiveMode.CYCLE_TRACKING) },
        )
        MahinChoiceChip(
            label = stringResource(R.string.onboarding_goal_ttc),
            selected = selectedMode == ReproductiveMode.TRYING_TO_CONCEIVE,
            onClick = { onModeSelected(ReproductiveMode.TRYING_TO_CONCEIVE) },
        )
        MahinChoiceChip(
            label = stringResource(R.string.onboarding_goal_pregnancy),
            selected = selectedMode == ReproductiveMode.PREGNANT,
            onClick = { onModeSelected(ReproductiveMode.PREGNANT) },
        )
        Spacer(modifier = Modifier.height(MahinSpacing.lg))
        MahinPrimaryButton(
            text = stringResource(R.string.onboarding_continue),
            onClick = onContinue,
        )
    }
}
