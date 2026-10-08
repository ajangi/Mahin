package dev.mahin.android.cycle

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.google.common.truth.Truth.assertThat
import dev.mahin.android.golden.M15GoldenFixtures
import dev.mahin.core.designsystem.MahinTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR", manifest = Config.NONE)
class TodayPregnancyCalendarEntryTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pregnancyToday_weekStripEntry_invokesCalendarAction() {
        var opened = false
        composeRule.setContent {
            MahinTheme {
                TodayScreenContent(
                    state = M15GoldenFixtures.todayPregnancyWeek(),
                    actions =
                        TodayScreenActions(
                            onOpenCalendar = { opened = true },
                        ),
                )
            }
        }
        composeRule
            .onNodeWithTag("today_screen_list")
            .performScrollToNode(hasTestTag("today_pregnancy_calendar_strip"))
        composeRule.onNodeWithTag("today_pregnancy_calendar_strip").performClick()
        assertThat(opened).isTrue()
    }
}
