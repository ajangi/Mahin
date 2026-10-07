package dev.mahin.android.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dev.mahin.android.navigation.MahinTopLevelDestination
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.model.ReproductiveMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR")
class ShellModeTransitionTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val stubOverrides =
        MahinShellScreenOverrides(
            today = { Box(Modifier.testTag("screen_today").fillMaxSize()) },
            calendar = { Box(Modifier.testTag("screen_calendar").fillMaxSize()) },
            log = { Box(Modifier.testTag("screen_log").fillMaxSize()) },
            cycleInsights = { Box(Modifier.testTag("screen_cycle_insights").fillMaxSize()) },
            ttcInsights = { Box(Modifier.testTag("screen_ttc_insights").fillMaxSize()) },
            pregnancyHub = { _, _ -> Box(Modifier.testTag("screen_pregnancy_hub").fillMaxSize()) },
            plan = { Box(Modifier.testTag("screen_plan").fillMaxSize()) },
            learn = { Box(Modifier.testTag("screen_learn").fillMaxSize()) },
            history = { Box(Modifier.testTag("screen_history").fillMaxSize()) },
            settings = { _, _, _ -> Box(Modifier.testTag("screen_settings").fillMaxSize()) },
        )

    @Test
    fun cycleToTtc_fromOrphanInsights_navigatesToTodayWithBars() =
        transition(
            from = ReproductiveMode.CYCLE_TRACKING,
            to = ReproductiveMode.TRYING_TO_CONCEIVE,
            startRoute = MahinTopLevelDestination.CycleInsights.route,
            expectScreen = "screen_today",
        )

    @Test
    fun ttcToCycle_fromOrphanTtcTab_navigatesToTodayWithBars() =
        transition(
            from = ReproductiveMode.TRYING_TO_CONCEIVE,
            to = ReproductiveMode.CYCLE_TRACKING,
            startRoute = MahinTopLevelDestination.TtcInsights.route,
            expectScreen = "screen_today",
        )

    @Test
    fun pregnantToCycle_fromPlan_navigatesToTodayWithBars() =
        transition(
            from = ReproductiveMode.PREGNANT,
            to = ReproductiveMode.CYCLE_TRACKING,
            startRoute = MahinTopLevelDestination.Plan.route,
            expectScreen = "screen_today",
        )

    @Test
    fun postPregnancyResumeToTtc_fromOrphanHub_navigatesToTodayWithBars() =
        transition(
            from = ReproductiveMode.POST_PREGNANCY_TRANSITION,
            to = ReproductiveMode.TRYING_TO_CONCEIVE,
            startRoute = MahinTopLevelDestination.PregnancyHub.route,
            expectScreen = "screen_today",
        )

    @Test
    fun pregnantToPostPregnancy_fromHub_navigatesToTodayWithBars() =
        transition(
            from = ReproductiveMode.PREGNANT,
            to = ReproductiveMode.POST_PREGNANCY_TRANSITION,
            startRoute = MahinTopLevelDestination.PregnancyHub.route,
            expectScreen = "screen_today",
        )

    @Test
    fun pausedToCycle_fromOrphanPlan_navigatesToTodayWithBars() =
        transition(
            from = ReproductiveMode.TRACKING_PAUSED,
            to = ReproductiveMode.CYCLE_TRACKING,
            startRoute = MahinTopLevelDestination.Plan.route,
            expectScreen = "screen_today",
        )

    private fun transition(
        from: ReproductiveMode,
        to: ReproductiveMode,
        startRoute: String,
        expectScreen: String,
    ) {
        val shellState = mutableStateOf(ShellNavigationState(profileLoaded = true, reproductiveMode = from))
        val navHolder = arrayOfNulls<NavHostController>(1)
        composeRule.setContent {
            MahinTheme {
                val navController = rememberNavController()
                navHolder[0] = navController
                MahinShellLayout(shellState.value, navController) {
                    MahinShellNavHost(
                        navController = navController,
                        onLocalDataErased = {},
                        onOpenHistory = {},
                        onOpenCycleCalendar = {},
                        screenOverrides = stubOverrides,
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { navHolder[0]!!.navigate(startRoute) }
        composeRule.waitForIdle()
        composeRule.runOnIdle { shellState.value = shellState.value.copy(reproductiveMode = to) }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(expectScreen).assertIsDisplayed()
        composeRule.onNodeWithTag("shell_bottom_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("shell_top_bar").assertIsDisplayed()
    }
}
