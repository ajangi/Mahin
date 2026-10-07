package dev.mahin.android.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import dev.mahin.android.navigation.MahinTopLevelDestination
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.designsystem.component.MahinBottomNavigationBar
import dev.mahin.core.model.ReproductiveMode
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR")
class MahinAppShellNavigationTest {
    @get:org.junit.Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomBar_navigatesTabs_andShowsSelectedState() {
        val destinations = MahinTopLevelDestination.forMode(ReproductiveMode.CYCLE_TRACKING)
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val tabs = destinations.map { it.toNavTab() }
                    val accent = reproductiveModeShellAccent(ReproductiveMode.CYCLE_TRACKING)
                    androidx.compose.material3.Scaffold(
                        bottomBar = {
                            MahinBottomNavigationBar(
                                tabs = tabs,
                                selectedRoute = navBackStackEntry?.destination?.route,
                                modeAccent = accent,
                                onTabSelected = { tab ->
                                    navController.navigate(tab.route) {
                                        launchSingleTop = true
                                    }
                                },
                            )
                        },
                    ) { padding ->
                        NavHost(
                            navController = navController,
                            startDestination = MahinTopLevelDestination.Today.route,
                            modifier = Modifier.padding(padding),
                        ) {
                            destinations.forEach { dest ->
                                composable(dest.route) {
                                    Box(Modifier.fillMaxSize().testTag("tab_${dest.route}")) {
                                        Text(dest.route)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        composeRule.onNodeWithTag("tab_today").assertIsDisplayed()
        composeRule.onNodeWithText("تقویم").performClick()
        composeRule.onNodeWithTag("tab_calendar").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("تقویم").assertIsSelected()
        composeRule.onNodeWithText("ثبت").performClick()
        composeRule.onNodeWithTag("tab_log").assertIsDisplayed()
    }

    @Test
    fun releaseManifest_excludesDebugDemoLauncher() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val component = launchIntent?.component?.className ?: ""
        assert(!component.contains("IconCatalogueActivity"))
        assert(!component.contains("demo"))
    }
}
