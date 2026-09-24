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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.mahin.android.demo.CalendarDemoScreen
import dev.mahin.android.demo.DesignSystemShowcaseScreen
import dev.mahin.android.demo.TodayPlaceholderScreen
import dev.mahin.android.navigation.MahinTopLevelDestination

@Composable
fun MahinAppShell(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val destinations = MahinTopLevelDestination.entries
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
            composable(MahinTopLevelDestination.Today.route) { TodayPlaceholderScreen() }
            composable(MahinTopLevelDestination.Calendar.route) { CalendarDemoScreen() }
            composable(MahinTopLevelDestination.Showcase.route) { DesignSystemShowcaseScreen() }
        }
    }
}
