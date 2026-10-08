package dev.mahin.android.cycle

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import dev.mahin.android.golden.M15GoldenFixtures
import dev.mahin.core.datetime.PersianDigits
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.testing.ViewModelStoreClearingRule
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR", manifest = Config.NONE)
class TodayScreenBehaviorTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val viewModelStoreRule = ViewModelStoreClearingRule(composeRule)

    @Test
    fun weekStripTap_updatesSelectedSemantics() {
        composeRule.setContent {
            MahinTheme {
                var state by remember { mutableStateOf(M15GoldenFixtures.todayFertileWindow()) }
                TodayScreenContent(
                    state = state,
                    onWeekDaySelected = { date ->
                        state =
                            state.copy(
                                weekStripWeeks =
                                    state.weekStripWeeks.map { week ->
                                        week.map { day ->
                                            day.copy(isSelected = day.date == date)
                                        }
                                    },
                                weekStrip =
                                    state.weekStrip.map { day ->
                                        day.copy(isSelected = day.date == date)
                                    },
                            )
                    },
                )
            }
        }
        val target = M15GoldenFixtures.todayFertileWindow().weekStripWeeks.first()[3]
        composeRule.onNodeWithTag("today_screen_list").performScrollToNode(hasTestTag("today_week_strip"))
        composeRule.onNodeWithText(PersianDigits.format(target.jalali.day)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(PersianDigits.format(target.jalali.day)).assertIsSelected()
    }

    @Test
    fun dailyTipSlot_whenVisible_rendersSlotContent() {
        composeRule.setContent {
            MahinTheme {
                TodayScreenContent(state = TodayUiState(showDailyTipSlot = true))
            }
        }
        composeRule.onNodeWithTag("today_daily_tip_slot").assertIsDisplayed()
    }

    @Test
    fun daySheet_showsLogLinesForSelectedDay() {
        val logLine = "خستگی · شاد"
        composeRule.setContent {
            MahinTheme {
                TodayScreenContent(
                    state =
                        TodayUiState(
                            todaySnapshot = M15GoldenFixtures.todayFertileWindow().todaySnapshot,
                            daySheetDate = LocalDate.of(2025, 3, 12),
                            daySheetMarkers = DayMarkers(),
                            daySheetLogLines = listOf(logLine),
                        ),
                )
            }
        }
        composeRule.onNodeWithTag("calendar_day_sheet_log_line").assertIsDisplayed()
        composeRule.onNodeWithText(logLine).assertIsDisplayed()
    }
}
