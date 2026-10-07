package dev.mahin.android.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dev.mahin.android.navigation.MahinTopLevelDestination

@Composable
fun MahinAppShell(
    modifier: Modifier = Modifier,
    onLocalDataErased: () -> Unit = {},
    shellViewModel: MahinAppShellViewModel = hiltViewModel(),
    screenOverrides: MahinShellScreenOverrides = MahinShellScreenOverrides.Default,
) {
    val navController = rememberNavController()
    val shellState by shellViewModel.navigationState.collectAsStateWithLifecycle()

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
