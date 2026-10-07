package dev.mahin.android.shell

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import dev.mahin.android.navigation.MahinTopLevelDestination
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.designsystem.component.MahinBottomNavigationBar
import dev.mahin.core.model.ReproductiveMode
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
    fun bottomBar_rtl_tabsInDeclaredOrder() {
        val destinations = MahinTopLevelDestination.forMode(ReproductiveMode.CYCLE_TRACKING)
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme {
                    MahinBottomNavigationBar(
                        tabs = destinations.map { it.toNavTab() },
                        selectedRoute = destinations.first().route,
                        modeAccent = reproductiveModeShellAccent(ReproductiveMode.CYCLE_TRACKING),
                        onTabSelected = {},
                    )
                }
            }
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val labels = destinations.map { context.getString(it.labelRes) }
        labels.forEach { label ->
            composeRule.onNodeWithText(label).assertIsDisplayed()
        }
        composeRule.onNodeWithText(labels.first()).assertHeightIsAtLeast(48.dp)
    }
}
