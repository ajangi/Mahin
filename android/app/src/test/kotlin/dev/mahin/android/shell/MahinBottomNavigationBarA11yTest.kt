package dev.mahin.android.shell

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.mahin.android.navigation.MahinTopLevelDestination
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.designsystem.component.MahinBottomNavigationBar
import dev.mahin.core.model.ReproductiveMode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR")
class MahinBottomNavigationBarA11yTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomBar_rtl_tabXPositionsDecreaseInForModeOrder() {
        assertTabOrder(LayoutDirection.Rtl) { left, nextLeft ->
            assertTrue("RTL tab x-positions should decrease in forMode order", left > nextLeft)
        }
    }

    @Test
    fun bottomBar_ltr_tabXPositionsIncreaseInForModeOrder() {
        assertTabOrder(LayoutDirection.Ltr) { left, nextLeft ->
            assertTrue("LTR tab x-positions should increase in forMode order", left < nextLeft)
        }
    }

    @Test
    fun bottomBar_cycleMode_everyTab_selectedStateAndMinTouchTarget() = assertModeTabs(ReproductiveMode.CYCLE_TRACKING)

    @Test
    fun bottomBar_ttcMode_everyTab_selectedStateAndMinTouchTarget() =
        assertModeTabs(ReproductiveMode.TRYING_TO_CONCEIVE)

    @Test
    fun bottomBar_pregnantMode_everyTab_selectedStateAndMinTouchTarget() = assertModeTabs(ReproductiveMode.PREGNANT)

    @Test
    fun bottomBar_postPregnancyMode_everyTab_selectedStateAndMinTouchTarget() =
        assertModeTabs(ReproductiveMode.POST_PREGNANCY_TRANSITION)

    @Test
    fun bottomBar_pausedMode_everyTab_selectedStateAndMinTouchTarget() =
        assertModeTabs(ReproductiveMode.TRACKING_PAUSED)

    private fun assertModeTabs(mode: ReproductiveMode) {
        val destinations = MahinTopLevelDestination.forMode(mode)
        var selectedRoute by mutableStateOf(destinations.first().route)
        composeRule.setContent {
            MahinTheme {
                MahinBottomNavigationBar(
                    tabs = destinations.map { it.toNavTab() },
                    selectedRoute = selectedRoute,
                    modeAccent = reproductiveModeShellAccent(mode),
                    onTabSelected = { tab -> selectedRoute = tab.route },
                )
            }
        }
        destinations.forEach { tab ->
            composeRule.onNodeWithTag("shell_tab_${tab.route}").performClick()
            destinations.forEach { candidate ->
                val node = composeRule.onNodeWithTag("shell_tab_${candidate.route}")
                node.assertIsDisplayed()
                node.assertHeightIsAtLeast(48.dp)
                if (candidate.route == tab.route) {
                    node.assertIsSelected()
                } else {
                    node.assertIsNotSelected()
                }
            }
        }
    }

    private fun assertTabOrder(
        direction: LayoutDirection,
        compare: (left: Float, nextLeft: Float) -> Unit,
    ) {
        val mode = ReproductiveMode.CYCLE_TRACKING
        val destinations = MahinTopLevelDestination.forMode(mode)
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                MahinTheme {
                    MahinBottomNavigationBar(
                        tabs = destinations.map { it.toNavTab() },
                        selectedRoute = destinations.first().route,
                        modeAccent = reproductiveModeShellAccent(mode),
                        onTabSelected = {},
                    )
                }
            }
        }
        val xPositions =
            destinations.map { destination ->
                composeRule
                    .onNodeWithTag("shell_tab_${destination.route}")
                    .getBoundsInRoot()
                    .left.value
            }
        for (index in 0 until xPositions.size - 1) {
            compare(xPositions[index], xPositions[index + 1])
        }
    }
}
