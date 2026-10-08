@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package dev.mahin.android.cycle

import androidx.activity.ComponentActivity
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import dev.mahin.android.golden.M15GoldenFixtures
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.testing.ViewModelStoreClearingRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR", manifest = Config.NONE)
class CalendarDaySheetBackTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val viewModelStoreRule = ViewModelStoreClearingRule(composeRule)

    @Test
    fun systemBack_closesDaySheet_andCalendarRemainsVisible() {
        var state by mutableStateOf(M15GoldenFixtures.calendarDaySheetOpen())
        composeRule.setContent {
            MahinTheme {
                SharedTransitionLayout {
                    CycleCalendarScreenContent(
                        state = state,
                        sharedTransitionScope = this,
                        onDateSelected = {},
                        onDismissDaySheet = { state = state.copy(daySheetOpen = false) },
                    )
                }
            }
        }
        composeRule.onNodeWithTag("calendar_day_sheet_overlay").assertExists()
        composeRule.onNodeWithTag("cycle_calendar_screen").assertExists()
        composeRule.runOnIdle {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("calendar_day_sheet_overlay").assertDoesNotExist()
        composeRule.onNodeWithTag("cycle_calendar_screen").assertExists()
    }
}
