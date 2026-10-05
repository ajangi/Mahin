package dev.mahin.android.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.designsystem.component.MahinJalaliDatePicker
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Regression: Jalali date picker inside [androidx.compose.foundation.verticalScroll] must not use an
 * unbounded lazy grid (see M1 handoff).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], manifest = Config.NONE, qualifiers = "fa-rIR")
class CalendarDemoScreenScrollTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun jalaliPicker_withVerticalScroll_composesWithoutCrash() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MahinTheme {
                    var selected by remember { mutableStateOf(JalaliDate(1403, 1, 1)) }
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(MahinSpacing.md),
                    ) {
                        MahinJalaliDatePicker(
                            selectedDate = selected,
                            onDateSelected = { selected = it },
                            initialVisibleMonth = selected,
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }
}
