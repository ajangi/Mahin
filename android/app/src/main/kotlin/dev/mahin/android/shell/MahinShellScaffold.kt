package dev.mahin.android.shell

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import dev.mahin.core.designsystem.MahinMotionDuration
import dev.mahin.core.designsystem.component.MahinBottomNavigationBar
import dev.mahin.core.designsystem.mahinMotionDurationMs
import dev.mahin.core.model.ReproductiveMode

@Composable
internal fun MahinShellBottomBar(
    reproductiveMode: ReproductiveMode,
    destinations: List<MahinTopLevelDestination>,
    selectedRoute: String?,
    onTabSelected: (MahinTopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    MahinBottomNavigationBar(
        modifier = modifier,
        tabs = destinations.map { it.toNavTab() },
        selectedRoute = selectedRoute,
        modeAccent = reproductiveModeShellAccent(reproductiveMode),
        onTabSelected = { tab ->
            val destination = destinations.first { it.route == tab.route }
            onTabSelected(destination)
        },
    )
}

@Suppress("LongParameterList")
@Composable
internal fun MahinShellNavHost(
    navController: NavHostController,
    onLocalDataErased: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenLearn: () -> Unit,
    onOpenCycleCalendar: () -> Unit,
    screenOverrides: MahinShellScreenOverrides = MahinShellScreenOverrides.Default,
) {
    val fadeMs = mahinMotionDurationMs(MahinMotionDuration.FAST_MS)
    val fadeSpec = tween<Float>(durationMillis = fadeMs)
    NavHost(
        navController = navController,
        startDestination = MahinTopLevelDestination.Today.route,
        enterTransition = { fadeIn(fadeSpec) },
        exitTransition = { fadeOut(fadeSpec) },
        popEnterTransition = { fadeIn(fadeSpec) },
        popExitTransition = { fadeOut(fadeSpec) },
    ) {
        composable(MahinTopLevelDestination.Today.route) {
            screenOverrides.today(onOpenHistory, onOpenLearn)
        }
        composable(MahinTopLevelDestination.Calendar.route) { screenOverrides.calendar() }
        composable(MahinTopLevelDestination.Log.route) { screenOverrides.log() }
        composable(MahinTopLevelDestination.CycleInsights.route) {
            screenOverrides.cycleInsights(onOpenHistory)
        }
        composable(MahinTopLevelDestination.TtcInsights.route) {
            screenOverrides.ttcInsights(onOpenHistory)
        }
        composable(MahinTopLevelDestination.PregnancyHub.route) {
            screenOverrides.pregnancyHub(onOpenHistory, onOpenCycleCalendar)
        }
        composable(MahinTopLevelDestination.Plan.route) { screenOverrides.plan() }
        composable(MahinTopLevelDestination.Learn.route) { screenOverrides.learn() }
        composable(MahinTopLevelDestination.HISTORY_ROUTE) { screenOverrides.history() }
        composable(MahinTopLevelDestination.SETTINGS_ROUTE) {
            screenOverrides.settings(
                { navController.navigateUp() },
                onOpenHistory,
                onLocalDataErased,
            )
        }
    }
}

/**
 * Production screens by default; tests supply tagged placeholders while keeping [MahinShellNavHost] routes.
 */
data class MahinShellScreenOverrides(
    val today: @Composable (onOpenHistory: () -> Unit, onOpenLearn: () -> Unit) -> Unit,
    val calendar: @Composable () -> Unit,
    val log: @Composable () -> Unit,
    val cycleInsights: @Composable (onOpenHistory: () -> Unit) -> Unit,
    val ttcInsights: @Composable (onOpenHistory: () -> Unit) -> Unit,
    val pregnancyHub: @Composable (onOpenHistory: () -> Unit, onOpenCycleCalendar: () -> Unit) -> Unit,
    val plan: @Composable () -> Unit,
    val learn: @Composable () -> Unit,
    val history: @Composable () -> Unit,
    val settings: @Composable (
        onNavigateUp: () -> Unit,
        onOpenHistory: () -> Unit,
        onLocalDataErased: () -> Unit,
    ) -> Unit,
) {
    companion object {
        val Default: MahinShellScreenOverrides =
            MahinShellScreenOverrides(
                today = { onOpenHistory, onOpenLearn ->
                    TodayScreen(
                        onOpenHistory = onOpenHistory,
                        onOpenLearn = onOpenLearn,
                    )
                },
                calendar = { CycleCalendarScreen() },
                log = { LogScreen() },
                cycleInsights = { onOpenHistory -> CycleInsightsScreen(onOpenHistory = onOpenHistory) },
                ttcInsights = { onOpenHistory -> TtcInsightsScreen(onOpenHistory = onOpenHistory) },
                pregnancyHub = { onOpenHistory, onOpenCycleCalendar ->
                    PregnancyHubScreen(
                        onOpenHistory = onOpenHistory,
                        onOpenCycleCalendar = onOpenCycleCalendar,
                    )
                },
                plan = { PregnancyPlanScreen() },
                learn = { LearnScreen() },
                history = { HistoryScreen() },
                settings = { onNavigateUp, onOpenHistory, onLocalDataErased ->
                    SettingsScreen(
                        onNavigateUp = onNavigateUp,
                        onOpenHistory = onOpenHistory,
                        onLocalDataErased = onLocalDataErased,
                    )
                },
            )
    }
}
