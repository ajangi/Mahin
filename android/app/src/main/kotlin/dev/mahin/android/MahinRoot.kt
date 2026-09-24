package dev.mahin.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.onboarding.OnboardingFlow
import dev.mahin.android.root.RootViewModel
import dev.mahin.android.shell.MahinAppShell
import dev.mahin.core.designsystem.component.MahinLoadingState

@Composable
fun MahinRoot(viewModel: RootViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.ensureGuestIdentity()
    }
    when {
        state.isLoading -> MahinLoadingState()
        !state.onboardingComplete -> OnboardingFlow(onComplete = viewModel::refreshOnboardingState)
        else -> MahinAppShell()
    }
}
