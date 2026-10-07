package dev.mahin.android.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import dev.mahin.android.navigation.MahinTopLevelDestination
import dev.mahin.android.navigation.ShellRoutePolicy
import dev.mahin.core.model.ReproductiveMode

@Composable
internal fun ShellModeRouteEffect(
    profileLoaded: Boolean,
    reproductiveMode: ReproductiveMode,
    currentRoute: String?,
    navController: NavHostController,
) {
    var lastMode by remember { mutableStateOf(reproductiveMode) }
    LaunchedEffect(reproductiveMode, profileLoaded, currentRoute) {
        if (!profileLoaded) return@LaunchedEffect
        val currentOrphan = ShellRoutePolicy.isOrphanTabRoute(currentRoute, reproductiveMode)
        val modeChanged = lastMode != reproductiveMode
        if (modeChanged) {
            val removedTabs =
                ShellRoutePolicy.tabRoutesForMode(lastMode) - ShellRoutePolicy.tabRoutesForMode(reproductiveMode)
            lastMode = reproductiveMode
            if (removedTabs.isNotEmpty()) {
                val stayRoute =
                    currentRoute?.takeIf { route ->
                        !ShellRoutePolicy.isOrphanTabRoute(route, reproductiveMode) &&
                            (
                                route in ShellRoutePolicy.tabRoutesForMode(reproductiveMode) ||
                                    ShellRoutePolicy.isSecondaryRoute(route, reproductiveMode)
                            )
                    }
                navController.navigate(MahinTopLevelDestination.Today.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = false
                    }
                    launchSingleTop = true
                }
                if (stayRoute != null && stayRoute != MahinTopLevelDestination.Today.route) {
                    navController.navigate(stayRoute) {
                        launchSingleTop = true
                        restoreState = stayRoute in ShellRoutePolicy.tabRoutesForMode(reproductiveMode)
                    }
                }
                return@LaunchedEffect
            }
        }
        if (currentOrphan) {
            navController.navigate(MahinTopLevelDestination.Today.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = false
                }
                launchSingleTop = true
            }
        }
    }
}
