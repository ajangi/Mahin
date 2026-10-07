package dev.mahin.android.shell

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.mahin.android.cycle.CycleCalendarScreen
import dev.mahin.android.cycle.HistoryScreen
import dev.mahin.android.cycle.LogScreen
import dev.mahin.android.cycle.TodayScreen
import dev.mahin.android.insights.CycleInsightsScreen
import dev.mahin.android.learn.LearnScreen
import dev.mahin.android.navigation.MahinTopLevelDestination
import dev.mahin.android.pregnancy.PregnancyHubScreen
import dev.mahin.android.pregnancy.PregnancyPlanScreen
import dev.mahin.android.settings.SettingsScreen
import dev.mahin.android.ttc.TtcInsightsScreen
import dev.mahin.core.designsystem.component.MahinBottomNavigationBar
import dev.mahin.core.designsystem.component.MahinShellSecondaryTopAppBar
import dev.mahin.core.designsystem.component.MahinShellTopAppBar
import dev.mahin.core.model.ReproductiveMode

@Composable
internal fun MahinShellTopBar(
    currentRoute: String?,
    topLevelRoutes: Set<String>,
    showBottomBar: Boolean,
    onNavigateUp: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    when (currentRoute) {
        MahinTopLevelDestination.HISTORY_ROUTE ->
            MahinShellSecondaryTopAppBar(
                title = stringResource(dev.mahin.android.R.string.history_title),
                onNavigateUp = onNavigateUp,
            )
        MahinTopLevelDestination.SETTINGS_ROUTE -> Unit
        in topLevelRoutes -> {
            val titleRes = MahinTopLevelDestination.titleResForRoute(currentRoute)
            if (titleRes != null) {
                MahinShellTopAppBar(
                    title = stringResource(titleRes),
                    onOpenSettings = onOpenSettings,
                )
            }
        }
        MahinTopLevelDestination.Calendar.route -> {
            if (!showBottomBar) {
                MahinShellSecondaryTopAppBar(
                    title = stringResource(dev.mahin.android.R.string.calendar_title),
                    onNavigateUp = onNavigateUp,
                )
            }
        }
    }
}

@Composable
internal fun MahinShellBottomBar(
    reproductiveMode: ReproductiveMode,
    destinations: List<MahinTopLevelDestination>,
    selectedRoute: String?,
    onTabSelected: (MahinTopLevelDestination) -> Unit,
) {
    MahinBottomNavigationBar(
        tabs = destinations.map { it.toNavTab() },
        selectedRoute = selectedRoute,
        modeAccent = reproductiveModeShellAccent(reproductiveMode),
        onTabSelected = { tab ->
            val destination = destinations.first { it.route == tab.route }
            onTabSelected(destination)
        },
    )
}

@Composable
internal fun MahinShellNavHost(
    navController: NavHostController,
    onLocalDataErased: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenCycleCalendar: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = MahinTopLevelDestination.Today.route,
    ) {
        composable(MahinTopLevelDestination.Today.route) {
            TodayScreen()
        }
        composable(MahinTopLevelDestination.Calendar.route) { CycleCalendarScreen() }
        composable(MahinTopLevelDestination.Log.route) { LogScreen() }
        composable(MahinTopLevelDestination.CycleInsights.route) {
            CycleInsightsScreen(onOpenHistory = onOpenHistory)
        }
        composable(MahinTopLevelDestination.TtcInsights.route) {
            TtcInsightsScreen(onOpenHistory = onOpenHistory)
        }
        composable(MahinTopLevelDestination.PregnancyHub.route) {
            PregnancyHubScreen(
                onOpenHistory = onOpenHistory,
                onOpenCycleCalendar = onOpenCycleCalendar,
            )
        }
        composable(MahinTopLevelDestination.Plan.route) { PregnancyPlanScreen() }
        composable(MahinTopLevelDestination.Learn.route) { LearnScreen() }
        composable(MahinTopLevelDestination.HISTORY_ROUTE) { HistoryScreen() }
        composable(MahinTopLevelDestination.SETTINGS_ROUTE) {
            SettingsScreen(
                onNavigateUp = { navController.navigateUp() },
                onOpenHistory = onOpenHistory,
                onLocalDataErased = onLocalDataErased,
            )
        }
    }
}
