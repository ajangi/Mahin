package dev.mahin.android.shell

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.mahin.android.navigation.MahinTopLevelDestination

@Composable
fun MahinAppShell(
    modifier: Modifier = Modifier,
    onLocalDataErased: () -> Unit = {},
    screenOverrides: MahinShellScreenOverrides = MahinShellScreenOverrides.Default,
) {
    val shellViewModel: MahinAppShellViewModel = hiltViewModel()
    val shellState by shellViewModel.navigationState.collectAsStateWithLifecycle()
    MahinAppShellContent(
        shellState = shellState,
        navController = rememberNavController(),
        modifier = modifier,
        onLocalDataErased = onLocalDataErased,
        screenOverrides = screenOverrides,
    )
}

@VisibleForTesting
@Composable
internal fun MahinAppShellWithNavigationOverride(
    shellNavigationStateOverride: ShellNavigationState,
    modifier: Modifier = Modifier,
    onLocalDataErased: () -> Unit = {},
    screenOverrides: MahinShellScreenOverrides = MahinShellScreenOverrides.Default,
) {
    MahinAppShellContent(
        shellState = shellNavigationStateOverride,
        navController = rememberNavController(),
        modifier = modifier,
        onLocalDataErased = onLocalDataErased,
        screenOverrides = screenOverrides,
    )
}

@Composable
internal fun MahinAppShellContent(
    shellState: ShellNavigationState,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLocalDataErased: () -> Unit = {},
    screenOverrides: MahinShellScreenOverrides = MahinShellScreenOverrides.Default,
) {
    val openHistory: () -> Unit = {
        navController.navigate(MahinTopLevelDestination.HISTORY_ROUTE) { launchSingleTop = true }
    }
    val openLearn: () -> Unit = {
        navController.navigate(MahinTopLevelDestination.Learn.route) { launchSingleTop = true }
    }
    val openCycleCalendar: () -> Unit = {
        navController.navigate(MahinTopLevelDestination.Calendar.route) { launchSingleTop = true }
    }

    MahinShellLayout(
        shellState = shellState,
        navController = navController,
        modifier = modifier,
        navHost = {
            MahinShellNavHost(
                navController = navController,
                onLocalDataErased = onLocalDataErased,
                onOpenHistory = openHistory,
                onOpenLearn = openLearn,
                onOpenCycleCalendar = openCycleCalendar,
                screenOverrides = screenOverrides,
            )
        },
    )
}
