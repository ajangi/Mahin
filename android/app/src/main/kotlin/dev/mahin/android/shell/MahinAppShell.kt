package dev.mahin.android.shell

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
    shellNavigationStateOverride: ShellNavigationState? = null,
) {
    val navController = rememberNavController()
    if (shellNavigationStateOverride != null) {
        MahinAppShellContent(
            shellState = shellNavigationStateOverride,
            navController = navController,
            modifier = modifier,
            onLocalDataErased = onLocalDataErased,
            screenOverrides = screenOverrides,
        )
    } else {
        val shellViewModel: MahinAppShellViewModel = hiltViewModel()
        val shellState by shellViewModel.navigationState.collectAsStateWithLifecycle()
        MahinAppShellContent(
            shellState = shellState,
            navController = navController,
            modifier = modifier,
            onLocalDataErased = onLocalDataErased,
            screenOverrides = screenOverrides,
        )
    }
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
                onOpenCycleCalendar = openCycleCalendar,
                screenOverrides = screenOverrides,
            )
        },
    )
}
