package dev.mahin.android.shell

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.mahin.android.cycle.CycleCalendarScreen
import dev.mahin.android.cycle.HistoryScreen
import dev.mahin.android.cycle.LogScreen
import dev.mahin.android.cycle.TodayScreen
import dev.mahin.android.navigation.MahinTopLevelDestination
import dev.mahin.android.pregnancy.PregnancyHubScreen
import dev.mahin.android.ttc.TtcInsightsScreen
import dev.mahin.core.model.ReproductiveMode

@Composable
fun MahinAppShell(
    modifier: Modifier = Modifier,
    shellViewModel: MahinAppShellViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val shellState by shellViewModel.navigationState.collectAsStateWithLifecycle()
    val destinations =
        if (!shellState.profileLoaded) {
            MahinTopLevelDestination.forMode(ReproductiveMode.CYCLE_TRACKING)
        } else {
            MahinTopLevelDestination.forMode(shellState.reproductiveMode)
        }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = stringResource(destination.labelRes),
                            )
                        },
                        label = { Text(text = stringResource(destination.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MahinTopLevelDestination.Today.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(MahinTopLevelDestination.Today.route) { TodayScreen() }
            composable(MahinTopLevelDestination.Calendar.route) { CycleCalendarScreen() }
            composable(MahinTopLevelDestination.Log.route) { LogScreen() }
            composable(MahinTopLevelDestination.TtcInsights.route) { TtcInsightsScreen() }
            composable(MahinTopLevelDestination.PregnancyHub.route) { PregnancyHubScreen() }
            composable(MahinTopLevelDestination.History.route) { HistoryScreen() }
        }
    }
}
