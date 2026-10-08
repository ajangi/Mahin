package dev.mahin.android.shell

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
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
class MahinShellNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun testOverrides(onSwitchToPregnant: (() -> Unit)? = null): MahinShellScreenOverrides =
        MahinShellScreenOverrides(
            today = { _, _, _, _, _, _ -> Box(Modifier.fillMaxSize().testTag("screen_today")) },
            calendar = { _ -> Box(Modifier.fillMaxSize().testTag("screen_calendar")) },
            log = {
                var note by rememberSaveable { mutableStateOf("") }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.testTag("log_state_field"),
                )
            },
            cycleInsights = { onOpenHistory ->
                Column {
                    Box(Modifier.testTag("screen_cycle_insights"))
                    Button(onClick = onOpenHistory, modifier = Modifier.testTag("open_history_cycle")) {
                        Text("history")
                    }
                }
            },
            ttcInsights = { onOpenHistory ->
                Column {
                    Box(Modifier.testTag("screen_ttc_insights"))
                    Button(onClick = onOpenHistory, modifier = Modifier.testTag("open_history_ttc")) {
                        Text("history")
                    }
                }
            },
            pregnancyHub = { onOpenHistory, onOpenCycleCalendar ->
                Column {
                    Box(Modifier.testTag("screen_pregnancy_hub"))
                    Button(onClick = onOpenHistory, modifier = Modifier.testTag("open_history_pregnancy")) {
                        Text("past")
                    }
                    Button(onClick = onOpenCycleCalendar, modifier = Modifier.testTag("open_calendar_pregnancy")) {
                        Text("calendar")
                    }
                }
            },
            plan = {
                var note by rememberSaveable { mutableStateOf("") }
                Column {
                    Box(Modifier.fillMaxSize().testTag("screen_plan"))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.testTag("plan_state_field"),
                    )
                }
            },
            learn = { Box(Modifier.fillMaxSize().testTag("screen_learn")) },
            history = { Box(Modifier.fillMaxSize().testTag("screen_history")) },
            settings = { onNavigateUp, onOpenHistory, _ ->
                Column {
                    Button(onClick = onNavigateUp, modifier = Modifier.testTag("settings_top_bar_up")) {
                        Text("up")
                    }
                    Box(Modifier.testTag("screen_settings"))
                    Button(onClick = onOpenHistory, modifier = Modifier.testTag("open_history_settings")) {
                        Text("data history")
                    }
                    if (onSwitchToPregnant != null) {
                        Button(onClick = onSwitchToPregnant, modifier = Modifier.testTag("settings_switch_pregnant")) {
                            Text("pregnant")
                        }
                    }
                }
            },
        )

    @Test
    fun shell_logTab_restoresTypedState() {
        val shellState =
            mutableStateOf(
                ShellNavigationState(
                    profileLoaded = true,
                    reproductiveMode = ReproductiveMode.CYCLE_TRACKING,
                ),
            )
        composeRule.setContent {
            MahinTheme {
                val navController = rememberNavController()
                MahinShellLayout(shellState.value, navController) {
                    MahinShellNavHost(
                        navController = navController,
                        onLocalDataErased = {},
                        onOpenHistory = {
                            navController.navigate(MahinTopLevelDestination.HISTORY_ROUTE)
                        },
                        onOpenLearn = {},
                        onOpenCycleCalendar = {
                            navController.navigate(MahinTopLevelDestination.Calendar.route)
                        },
                        onOpenLogTab = {},
                        onOpenPlan = {},
                        onOpenPregnancyTab = {},
                        screenOverrides = testOverrides(),
                    )
                }
            }
        }
        composeRule.onNodeWithText("ثبت").performClick()
        composeRule.onNodeWithTag("log_state_field").performTextInput("a")
        composeRule.onNodeWithText("امروز").performClick()
        composeRule.onNodeWithText("ثبت").performClick()
        composeRule.onNodeWithTag("log_state_field").assertTextContains("a")
    }

    @Test
    fun shell_historyReachable_fromCycleInsights() {
        mountShell(ReproductiveMode.CYCLE_TRACKING, MahinTopLevelDestination.CycleInsights.route)
        composeRule.onNodeWithTag("open_history_cycle").performClick()
        composeRule.onNodeWithTag("screen_history").assertIsDisplayed()
        composeRule.onNodeWithTag("shell_top_bar_up").assertIsDisplayed()
    }

    @Test
    fun shell_historyReachable_fromTtcInsights() {
        mountShell(ReproductiveMode.TRYING_TO_CONCEIVE, MahinTopLevelDestination.TtcInsights.route)
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_ttc_insights").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("open_history_ttc").performClick()
        composeRule.onNodeWithTag("screen_history").assertIsDisplayed()
        composeRule.onNodeWithTag("shell_top_bar_up").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_ttc_insights").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun shell_historyReachable_fromSettings() {
        mountShell(ReproductiveMode.CYCLE_TRACKING, MahinTopLevelDestination.SETTINGS_ROUTE)
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_settings").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("open_history_settings").performClick()
        composeRule.onNodeWithTag("screen_history").assertIsDisplayed()
        composeRule.onNodeWithTag("shell_top_bar_up").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_settings").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun shell_history_systemBack_returnsToPreviousScreen() {
        mountShell(ReproductiveMode.CYCLE_TRACKING, MahinTopLevelDestination.CycleInsights.route)
        composeRule.onNodeWithTag("open_history_cycle").performClick()
        composeRule.onNodeWithTag("screen_history").assertIsDisplayed()
        pressSystemBack()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_cycle_insights").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun shell_settings_systemBack_returnsToTab() {
        mountShell(ReproductiveMode.CYCLE_TRACKING, MahinTopLevelDestination.Today.route)
        composeRule.onNodeWithTag("shell_open_settings").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_settings").fetchSemanticsNodes().isNotEmpty()
        }
        pressSystemBack()
        composeRule.onNodeWithTag("screen_today").assertIsDisplayed()
    }

    @Test
    fun shell_pregnancyHistoryAndCalendar_backWithUp() {
        val shellState =
            mutableStateOf(
                ShellNavigationState(
                    profileLoaded = true,
                    reproductiveMode = ReproductiveMode.PREGNANT,
                ),
            )
        val navHolder = arrayOfNulls<NavHostController>(1)
        composeRule.setContent {
            MahinTheme {
                val navController = rememberNavController()
                navHolder[0] = navController
                MahinShellLayout(shellState.value, navController) {
                    MahinShellNavHost(
                        navController = navController,
                        onLocalDataErased = {},
                        onOpenHistory = {
                            navController.navigate(MahinTopLevelDestination.HISTORY_ROUTE)
                        },
                        onOpenLearn = {},
                        onOpenCycleCalendar = {
                            navController.navigate(MahinTopLevelDestination.Calendar.route)
                        },
                        onOpenLogTab = {},
                        onOpenPlan = {},
                        onOpenPregnancyTab = {},
                        screenOverrides = testOverrides(),
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            navHolder[0]!!.navigate(MahinTopLevelDestination.PregnancyHub.route)
        }
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_pregnancy_hub").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("open_history_pregnancy").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_history").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("screen_history").assertIsDisplayed()
        composeRule.onNodeWithTag("shell_top_bar_up").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_pregnancy_hub").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("open_calendar_pregnancy").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_calendar").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("screen_calendar").assertIsDisplayed()
        composeRule.onNodeWithTag("shell_bottom_bar").assertDoesNotExist()
        composeRule.onNodeWithTag("shell_top_bar_up").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_pregnancy_hub").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun shell_pregnancyCalendar_systemBack_returnsToHub() {
        val shellState =
            mutableStateOf(
                ShellNavigationState(
                    profileLoaded = true,
                    reproductiveMode = ReproductiveMode.PREGNANT,
                ),
            )
        val navHolder = arrayOfNulls<NavHostController>(1)
        composeRule.setContent {
            MahinTheme {
                val navController = rememberNavController()
                navHolder[0] = navController
                MahinShellLayout(shellState.value, navController) {
                    MahinShellNavHost(
                        navController = navController,
                        onLocalDataErased = {},
                        onOpenHistory = {
                            navController.navigate(MahinTopLevelDestination.HISTORY_ROUTE)
                        },
                        onOpenLearn = {},
                        onOpenCycleCalendar = {
                            navController.navigate(MahinTopLevelDestination.Calendar.route)
                        },
                        onOpenLogTab = {},
                        onOpenPlan = {},
                        onOpenPregnancyTab = {},
                        screenOverrides = testOverrides(),
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { navHolder[0]!!.navigate(MahinTopLevelDestination.PregnancyHub.route) }
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_pregnancy_hub").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("open_calendar_pregnancy").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_calendar").fetchSemanticsNodes().isNotEmpty()
        }
        pressSystemBack()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_pregnancy_hub").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun mahinAppShell_cycleInsightsHistoryButton_opensHistoryRoute() {
        composeRule.setContent {
            MahinTheme {
                MahinAppShellWithNavigationOverride(
                    shellNavigationStateOverride =
                        ShellNavigationState(
                            profileLoaded = true,
                            reproductiveMode = ReproductiveMode.CYCLE_TRACKING,
                        ),
                    screenOverrides = testOverrides(),
                )
            }
        }
        composeRule.onNodeWithText("تحلیل‌ها").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_cycle_insights").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("open_history_cycle").performClick()
        composeRule.onNodeWithTag("screen_history").assertIsDisplayed()
    }

    @Test
    fun mahinAppShell_pregnancyHubCalendarButton_opensCalendarSecondary() {
        composeRule.setContent {
            MahinTheme {
                MahinAppShellWithNavigationOverride(
                    shellNavigationStateOverride =
                        ShellNavigationState(
                            profileLoaded = true,
                            reproductiveMode = ReproductiveMode.PREGNANT,
                        ),
                    screenOverrides = testOverrides(),
                )
            }
        }
        composeRule.onNodeWithText("بارداری").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_pregnancy_hub").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("open_calendar_pregnancy").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_calendar").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("screen_calendar").assertIsDisplayed()
        composeRule.onNodeWithTag("shell_bottom_bar").assertDoesNotExist()
    }

    @Test
    fun modeChange_fromCycleInsightsViaSettingsToPregnant_leavesValidTab() {
        val shellState =
            mutableStateOf(
                ShellNavigationState(
                    profileLoaded = true,
                    reproductiveMode = ReproductiveMode.CYCLE_TRACKING,
                ),
            )
        composeRule.setContent {
            MahinTheme {
                val navController = rememberNavController()
                MahinShellLayout(shellState.value, navController) {
                    MahinShellNavHost(
                        navController = navController,
                        onLocalDataErased = {},
                        onOpenHistory = {},
                        onOpenLearn = {},
                        onOpenCycleCalendar = {},
                        onOpenLogTab = {},
                        onOpenPlan = {},
                        onOpenPregnancyTab = {},
                        screenOverrides =
                            testOverrides(
                                onSwitchToPregnant = {
                                    shellState.value =
                                        shellState.value.copy(reproductiveMode = ReproductiveMode.PREGNANT)
                                },
                            ),
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("تحلیل‌ها").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_cycle_insights").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("shell_open_settings").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_settings").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("settings_switch_pregnant").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_top_bar_up").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_today").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("screen_today").assertIsDisplayed()
        composeRule.onNodeWithTag("shell_bottom_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("shell_top_bar").assertIsDisplayed()
    }

    @Test
    fun modeChange_fromPregnancyHubToPostPregnancy_showsBarsOnToday() {
        val shellState =
            mutableStateOf(
                ShellNavigationState(
                    profileLoaded = true,
                    reproductiveMode = ReproductiveMode.PREGNANT,
                ),
            )
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
                        onOpenLearn = {},
                        onOpenCycleCalendar = {},
                        onOpenLogTab = {},
                        onOpenPlan = {},
                        onOpenPregnancyTab = {},
                        screenOverrides = testOverrides(),
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            navHolder[0]!!.navigate(MahinTopLevelDestination.PregnancyHub.route)
        }
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_pregnancy_hub").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.runOnIdle {
            shellState.value =
                shellState.value.copy(reproductiveMode = ReproductiveMode.POST_PREGNANCY_TRANSITION)
        }
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("screen_today").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("shell_bottom_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("shell_top_bar").assertIsDisplayed()
    }

    @Test
    fun modeChange_pregnantToCycle_clearsRemovedPlanTabSaveable() {
        val shellState =
            mutableStateOf(
                ShellNavigationState(
                    profileLoaded = true,
                    reproductiveMode = ReproductiveMode.PREGNANT,
                ),
            )
        composeRule.setContent {
            MahinTheme {
                val navController = rememberNavController()
                MahinShellLayout(shellState.value, navController) {
                    MahinShellNavHost(
                        navController = navController,
                        onLocalDataErased = {},
                        onOpenHistory = {},
                        onOpenLearn = {},
                        onOpenCycleCalendar = {},
                        onOpenLogTab = {},
                        onOpenPlan = {},
                        onOpenPregnancyTab = {},
                        screenOverrides = testOverrides(),
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("برنامه").performClick()
        composeRule.onNodeWithTag("plan_state_field").performTextInput("saved")
        composeRule.onNodeWithText("امروز").performClick()
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            shellState.value = shellState.value.copy(reproductiveMode = ReproductiveMode.CYCLE_TRACKING)
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            shellState.value = shellState.value.copy(reproductiveMode = ReproductiveMode.PREGNANT)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("برنامه").performClick()
        composeRule.onNodeWithTag("plan_state_field").assertTextEquals("")
    }

    @Test
    fun planTabSaveable_restoresTextAfterTodaySwitchWithoutModeChange() {
        composeRule.setContent {
            MahinTheme {
                val navController = rememberNavController()
                MahinShellLayout(
                    ShellNavigationState(
                        profileLoaded = true,
                        reproductiveMode = ReproductiveMode.PREGNANT,
                    ),
                    navController,
                ) {
                    MahinShellNavHost(
                        navController = navController,
                        onLocalDataErased = {},
                        onOpenHistory = {},
                        onOpenLearn = {},
                        onOpenCycleCalendar = {},
                        onOpenLogTab = {},
                        onOpenPlan = {},
                        onOpenPregnancyTab = {},
                        screenOverrides = testOverrides(),
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("برنامه").performClick()
        composeRule.onNodeWithTag("plan_state_field").performTextInput("saved")
        composeRule.onNodeWithText("امروز").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("برنامه").performClick()
        composeRule.onNodeWithTag("plan_state_field").assertTextContains("saved")
    }

    @Test
    fun shell_bottomBarTabs_exposeSelectedState() {
        mountShell(ReproductiveMode.CYCLE_TRACKING, MahinTopLevelDestination.Today.route)
        composeRule.onNodeWithTag("shell_tab_today").assertIsSelected()
        composeRule.onNodeWithTag("shell_tab_calendar").performClick()
        composeRule.onNodeWithTag("shell_tab_calendar").assertIsSelected()
    }

    @Test
    fun shell_tabs_meetMinimumTouchTarget() {
        mountShell(ReproductiveMode.CYCLE_TRACKING, MahinTopLevelDestination.Today.route)
        composeRule.onNodeWithTag("shell_tab_today").assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun shell_profileNotLoaded_showsLoadingNotWrongTabs() {
        composeRule.setContent {
            MahinTheme {
                val navController = rememberNavController()
                MahinShellLayout(
                    ShellNavigationState(profileLoaded = false),
                    navController,
                ) {
                    Box(Modifier.testTag("should_not_show"))
                }
            }
        }
        composeRule.onNodeWithTag("shell_loading").assertIsDisplayed()
        composeRule.onNodeWithTag("should_not_show").assertDoesNotExist()
    }

    private fun pressSystemBack() {
        composeRule.activity.onBackPressedDispatcher.onBackPressed()
        composeRule.waitForIdle()
    }

    private fun mountShell(
        mode: ReproductiveMode,
        startRoute: String,
    ) {
        val navHolder = arrayOfNulls<NavHostController>(1)
        composeRule.setContent {
            MahinTheme {
                val navController = rememberNavController()
                navHolder[0] = navController
                MahinShellLayout(
                    ShellNavigationState(profileLoaded = true, reproductiveMode = mode),
                    navController,
                ) {
                    MahinShellNavHost(
                        navController = navController,
                        onLocalDataErased = {},
                        onOpenHistory = {
                            navController.navigate(MahinTopLevelDestination.HISTORY_ROUTE)
                        },
                        onOpenLearn = {},
                        onOpenCycleCalendar = {
                            navController.navigate(MahinTopLevelDestination.Calendar.route)
                        },
                        onOpenLogTab = {},
                        onOpenPlan = {},
                        onOpenPregnancyTab = {},
                        screenOverrides = testOverrides(),
                    )
                }
            }
        }
        composeRule.waitForIdle()
        if (startRoute != MahinTopLevelDestination.Today.route) {
            composeRule.runOnIdle { navHolder[0]!!.navigate(startRoute) }
            composeRule.waitForIdle()
        }
    }
}
