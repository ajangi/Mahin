package dev.mahin.android.shell

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import dev.mahin.android.cycle.editor.DayEditorCoordinator
import dev.mahin.android.cycle.editor.DayEditorHost
import dev.mahin.android.cycle.editor.DayEditorSession
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
@Suppress("LongParameterList")
internal fun MahinAppShellWithNavigationOverride(
    shellNavigationStateOverride: ShellNavigationState,
    modifier: Modifier = Modifier,
    onLocalDataErased: () -> Unit = {},
    screenOverrides: MahinShellScreenOverrides = MahinShellScreenOverrides.Default,
    dayEditorOverlay: @Composable (DayEditorSession?, () -> Unit) -> Unit = { session, onDismiss ->
        DayEditorHost(session = session, onDismiss = onDismiss)
    },
    dayEditorCoordinatorOverride: DayEditorCoordinator? = null,
) {
    MahinAppShellContent(
        shellState = shellNavigationStateOverride,
        navController = rememberNavController(),
        modifier = modifier,
        onLocalDataErased = onLocalDataErased,
        screenOverrides = screenOverrides,
        dayEditorOverlay = dayEditorOverlay,
        dayEditorCoordinatorOverride = dayEditorCoordinatorOverride,
    )
}

@Composable
@Suppress("LongParameterList")
internal fun MahinAppShellContent(
    shellState: ShellNavigationState,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLocalDataErased: () -> Unit = {},
    screenOverrides: MahinShellScreenOverrides = MahinShellScreenOverrides.Default,
    dayEditorOverlay: @Composable (DayEditorSession?, () -> Unit) -> Unit = { session, onDismiss ->
        DayEditorHost(session = session, onDismiss = onDismiss)
    },
    dayEditorCoordinatorOverride: DayEditorCoordinator? = null,
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
    val navigationEntryPoint =
        remember(context) {
            EntryPointAccessors.fromApplication(context, AppNavigationEntryPoint::class.java)
        }
    val logTabDateRequest = remember(navigationEntryPoint) { navigationEntryPoint.logTabDateRequest() }
    val dayEditorCoordinator =
        dayEditorCoordinatorOverride
            ?: remember(navigationEntryPoint) { navigationEntryPoint.dayEditorCoordinator() }
    val dayEditorSession by dayEditorCoordinator.session.collectAsStateWithLifecycle()
    val openDayEditor: (LocalDate) -> Unit = { date -> dayEditorCoordinator.open(date) }
    val openLogTab: (LocalDate?) -> Unit = { date ->
        if (date != null) {
            logTabDateRequest.request(date)
        }
        navController.navigate(MahinTopLevelDestination.Log.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
        if (date != null) {
            dayEditorCoordinator.open(date)
        }
    }
    val openPlan: () -> Unit = {
        navController.navigate(MahinTopLevelDestination.Plan.route) { launchSingleTop = true }
    }
    val openPregnancyTab: () -> Unit = {
        navController.navigate(MahinTopLevelDestination.PregnancyHub.route) { launchSingleTop = true }
    }
    val openPregnancyWeekByWeek: () -> Unit = {
        navController.navigate(MahinTopLevelDestination.PREGNANCY_WEEK_BY_WEEK_ROUTE) { launchSingleTop = true }
    }
    val openPregnancyTools: () -> Unit = {
        navController.navigate(MahinTopLevelDestination.PREGNANCY_TOOLS_ROUTE) { launchSingleTop = true }
    }
    val openPregnancyChecklist: () -> Unit = {
        navController.navigate(MahinTopLevelDestination.PREGNANCY_CHECKLIST_ROUTE) { launchSingleTop = true }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MahinShellLayout(
            shellState = shellState,
            navController = navController,
            navHost = {
                MahinShellNavHost(
                    navController = navController,
                    onLocalDataErased = onLocalDataErased,
                    onOpenHistory = openHistory,
                    onOpenLearn = openLearn,
                    onOpenCycleCalendar = openCycleCalendar,
                    onOpenLogTab = openLogTab,
                    onOpenDayEditor = openDayEditor,
                    onOpenPlan = openPlan,
                    onOpenPregnancyTab = openPregnancyTab,
                    onOpenPregnancyWeekByWeek = openPregnancyWeekByWeek,
                    onOpenPregnancyTools = openPregnancyTools,
                    onOpenPregnancyChecklist = openPregnancyChecklist,
                    screenOverrides = screenOverrides,
                )
            },
        )
        dayEditorOverlay(dayEditorSession, dayEditorCoordinator::dismiss)
    }
}
