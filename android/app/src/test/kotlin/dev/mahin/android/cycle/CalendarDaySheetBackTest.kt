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
    private val androidComposeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val composeRule = ViewModelStoreClearingRule.withCompose(androidComposeRule)

    @Test
    fun systemBack_closesDaySheet_andCalendarRemainsVisible() {
        var state by mutableStateOf(M15GoldenFixtures.calendarDaySheetOpen())
        androidComposeRule.setContent {
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
        androidComposeRule.onNodeWithTag("calendar_day_sheet_overlay").assertExists()
        androidComposeRule.onNodeWithTag("cycle_calendar_screen").assertExists()
        androidComposeRule.runOnIdle {
            androidComposeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        androidComposeRule.waitForIdle()
        androidComposeRule.onNodeWithTag("calendar_day_sheet_overlay").assertDoesNotExist()
        androidComposeRule.onNodeWithTag("cycle_calendar_screen").assertExists()
    }
}
