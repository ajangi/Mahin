package dev.mahin.android.shell

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.mahin.android.navigation.MahinTopLevelDestination
import dev.mahin.core.model.ReproductiveMode

@Composable
fun MahinAppShell(
    modifier: Modifier = Modifier,
    onLocalDataErased: () -> Unit = {},
    shellViewModel: MahinAppShellViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val shellState by shellViewModel.navigationState.collectAsStateWithLifecycle()
    val reproductiveMode =
        if (!shellState.profileLoaded) {
            ReproductiveMode.CYCLE_TRACKING
        } else {
            shellState.reproductiveMode
        }
    val destinations = MahinTopLevelDestination.forMode(reproductiveMode)
    val topLevelRoutes = destinations.map { it.route }.toSet()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in topLevelRoutes

    val openHistory: () -> Unit = {
        navController.navigate(MahinTopLevelDestination.HISTORY_ROUTE) { launchSingleTop = true }
    }
    val openSettings: () -> Unit = {
        navController.navigate(MahinTopLevelDestination.SETTINGS_ROUTE) { launchSingleTop = true }
    }
    val openCycleCalendar: () -> Unit = {
        navController.navigate(MahinTopLevelDestination.Calendar.route) { launchSingleTop = true }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            MahinShellTopBar(
                currentRoute = currentRoute,
                topLevelRoutes = topLevelRoutes,
                showBottomBar = showBottomBar,
                onNavigateUp = { navController.navigateUp() },
                onOpenSettings = openSettings,
            )
        },
        bottomBar = {
            if (showBottomBar) {
                MahinShellBottomBar(
                    reproductiveMode = reproductiveMode,
                    destinations = destinations,
                    selectedRoute = currentRoute,
                    onTabSelected = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(Modifier.padding(innerPadding)) {
            MahinShellNavHost(
                navController = navController,
                onLocalDataErased = onLocalDataErased,
                onOpenHistory = openHistory,
                onOpenCycleCalendar = openCycleCalendar,
            )
        }
    }
}
