package dev.mahin.android.learn

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing

@Composable
fun GuidedPlayerScreenContent(
    state: GuidedPlayerUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(state.stepIndex) {
        if (!state.completed) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(MahinSpacing.md)
                .testTag("learn_guided_player_screen"),
    ) {
        if (state.loadError) {
            Text(
                text =
                    stringResource(
                        if (state.offline) {
                            R.string.learn_player_offline
                        } else {
                            R.string.learn_player_load_error
                        },
                    ),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.testTag("learn_guided_player_error"),
            )
            return
        }
        Text(text = state.sessionTitle, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = stringResource(R.string.learn_player_step_label, state.stepIndex + 1, state.stepCount),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = MahinSpacing.sm),
        )
        LinearProgressIndicator(
            progress = { state.progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().padding(vertical = MahinSpacing.md),
        )
        Text(text = state.stepInstruction, style = MaterialTheme.typography.titleLarge)
        if (state.completed) {
            Button(onClick = onComplete, modifier = Modifier.padding(top = MahinSpacing.lg)) {
                Text(stringResource(R.string.learn_player_finish))
            }
        } else if (state.paused) {
            Button(onClick = onResume, modifier = Modifier.padding(top = MahinSpacing.lg)) {
                Text(stringResource(R.string.learn_player_resume))
            }
        } else {
            Button(onClick = onPause, modifier = Modifier.padding(top = MahinSpacing.lg)) {
                Text(stringResource(R.string.learn_player_pause))
            }
        }
    }
}
