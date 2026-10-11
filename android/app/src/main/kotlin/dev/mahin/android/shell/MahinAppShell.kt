package dev.mahin.android.shell

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.EntryPointAccessors
import dev.mahin.android.navigation.AppNavigationEntryPoint
import dev.mahin.android.navigation.MahinTopLevelDestination
import java.time.LocalDate

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
    val context = LocalContext.current.applicationContext
    val logTabDateRequest =
        remember(context) {
            EntryPointAccessors
                .fromApplication(context, AppNavigationEntryPoint::class.java)
                .logTabDateRequest()
        }
    val openLogTab: (LocalDate?) -> Unit = { date ->
        if (date != null) {
            logTabDateRequest.request(date)
        }
        navController.navigate(MahinTopLevelDestination.Log.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    val openPlan: () -> Unit = {
        navController.navigate(MahinTopLevelDestination.Plan.route) { launchSingleTop = true }
    }
    val openPregnancyTab: () -> Unit = {
        navController.navigate(MahinTopLevelDestination.PregnancyHub.route) { launchSingleTop = true }
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
                onOpenLogTab = openLogTab,
                onOpenPlan = openPlan,
                onOpenPregnancyTab = openPregnancyTab,
                screenOverrides = screenOverrides,
            )
        },
    )
}
