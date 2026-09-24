package dev.mahin.android.demo

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import dev.mahin.core.designsystem.MahinTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Regression: [CalendarDemoScreen] wraps [dev.mahin.core.designsystem.component.MahinJalaliDatePicker]
 * in [androidx.compose.foundation.verticalScroll]. A lazy grid with unbounded max height crashes;
 * the month grid must use a bounded non-lazy layout.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], manifest = Config.NONE, qualifiers = "fa-rIR")
class CalendarDemoScreenScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun calendarDemoScreen_withVerticalScroll_composesWithoutCrash() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme {
                    CalendarDemoScreen()
                }
            }
        }
        composeRule.waitForIdle()
    }
}
