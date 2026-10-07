package dev.mahin.android.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.mahin.android.navigation.MahinTopLevelDestination
import dev.mahin.android.navigation.ShellRoutePolicy
import dev.mahin.core.designsystem.component.MahinLoadingState
import dev.mahin.core.designsystem.component.MahinShellSecondaryTopAppBar
import dev.mahin.core.designsystem.component.MahinShellTopAppBar
import dev.mahin.core.model.ReproductiveMode

@Composable
fun MahinShellLayout(
    shellState: ShellNavigationState,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    navHost: @Composable () -> Unit,
) {
    if (!shellState.profileLoaded) {
        MahinLoadingState(
            modifier = modifier.fillMaxSize().testTag("shell_loading"),
        )
        return
    }
    val reproductiveMode = shellState.reproductiveMode
    val destinations = MahinTopLevelDestination.forMode(reproductiveMode)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = ShellRoutePolicy.showsShellBottomBar(currentRoute, reproductiveMode)

    ShellModeRouteEffect(
        profileLoaded = shellState.profileLoaded,
        reproductiveMode = reproductiveMode,
        currentRoute = currentRoute,
        navController = navController,
    )

    val openSettings: () -> Unit = {
        navController.navigate(MahinTopLevelDestination.SETTINGS_ROUTE) { launchSingleTop = true }
    }

    Scaffold(
        modifier = modifier.testTag("shell_scaffold"),
        topBar = {
            MahinShellTopBar(
                currentRoute = currentRoute,
                reproductiveMode = reproductiveMode,
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
                    modifier = Modifier.testTag("shell_bottom_bar"),
                )
            }
        },
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding).fillMaxSize()) {
            navHost()
        }
    }
}

@Composable
internal fun MahinShellTopBar(
    currentRoute: String?,
    reproductiveMode: ReproductiveMode,
    showBottomBar: Boolean,
    onNavigateUp: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    when {
        currentRoute == MahinTopLevelDestination.SETTINGS_ROUTE -> Unit
        showBottomBar -> {
            val titleRes = MahinTopLevelDestination.titleResForRoute(currentRoute)
            if (titleRes != null) {
                MahinShellTopAppBar(
                    title = stringResource(titleRes),
                    onOpenSettings = onOpenSettings,
                    modifier = Modifier.testTag("shell_top_bar"),
                )
            }
        }
        else -> {
            val titleRes =
                ShellRoutePolicy.shellTitleRes(
                    route = currentRoute,
                    mode = reproductiveMode,
                    showBottomBar = showBottomBar,
                )
            if (titleRes != null) {
                MahinShellSecondaryTopAppBar(
                    title = stringResource(titleRes),
                    onNavigateUp = onNavigateUp,
                    navigationIconTestTag = "shell_top_bar_up",
                )
            } else if (ShellRoutePolicy.isOrphanTabRoute(currentRoute, reproductiveMode)) {
                val fallbackRes = MahinTopLevelDestination.titleResForRoute(currentRoute)
                if (fallbackRes != null) {
                    MahinShellSecondaryTopAppBar(
                        title = stringResource(fallbackRes),
                        onNavigateUp = onNavigateUp,
                        navigationIconTestTag = "shell_top_bar_up",
                    )
                }
            }
        }
    }
}
