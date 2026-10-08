@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package dev.mahin.android.golden

import androidx.activity.ComponentActivity
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.mahin.android.cycle.CycleCalendarScreenContent
import dev.mahin.android.cycle.TodayScreenContent
import dev.mahin.core.testing.ViewModelStoreClearingRule
import dev.mahin.core.testing.roborazzi.captureMahinFullScreenGolden
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "fa-rIR-w411dp-h891dp-xxhdpi")
class M15FullScreenGoldenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val viewModelStoreRule = ViewModelStoreClearingRule(composeRule)

    @Test fun today_firstDay_light_scale10() = captureToday(M15GoldenFixtures.todayFirstDay(), false, 1f)

    @Test fun today_firstDay_light_scale13() = captureToday(M15GoldenFixtures.todayFirstDay(), false, 1.3f)

    @Test fun today_firstDay_dark_scale10() = captureToday(M15GoldenFixtures.todayFirstDay(), true, 1f)

    @Test fun today_firstDay_dark_scale13() = captureToday(M15GoldenFixtures.todayFirstDay(), true, 1.3f)

    @Test fun today_earlyCycle_light_scale10() = captureToday(M15GoldenFixtures.todayEarlyCycle(), false, 1f)

    @Test fun today_earlyCycle_light_scale13() = captureToday(M15GoldenFixtures.todayEarlyCycle(), false, 1.3f)

    @Test fun today_earlyCycle_dark_scale10() = captureToday(M15GoldenFixtures.todayEarlyCycle(), true, 1f)

    @Test fun today_earlyCycle_dark_scale13() = captureToday(M15GoldenFixtures.todayEarlyCycle(), true, 1.3f)

    @Test fun today_fertileWindow_light_scale10() = captureToday(M15GoldenFixtures.todayFertileWindow(), false, 1f)

    @Test fun today_fertileWindow_light_scale13() = captureToday(M15GoldenFixtures.todayFertileWindow(), false, 1.3f)

    @Test fun today_fertileWindow_dark_scale10() = captureToday(M15GoldenFixtures.todayFertileWindow(), true, 1f)

    @Test fun today_fertileWindow_dark_scale13() = captureToday(M15GoldenFixtures.todayFertileWindow(), true, 1.3f)

    @Test fun today_overdue_light_scale10() = captureToday(M15GoldenFixtures.todayOverdue(), false, 1f)

    @Test fun today_overdue_light_scale13() = captureToday(M15GoldenFixtures.todayOverdue(), false, 1.3f)

    @Test fun today_overdue_dark_scale10() = captureToday(M15GoldenFixtures.todayOverdue(), true, 1f)

    @Test fun today_overdue_dark_scale13() = captureToday(M15GoldenFixtures.todayOverdue(), true, 1.3f)

    @Test fun today_lowConfidence_light_scale10() = captureToday(M15GoldenFixtures.todayLowConfidence(), false, 1f)

    @Test fun today_lowConfidence_light_scale13() = captureToday(M15GoldenFixtures.todayLowConfidence(), false, 1.3f)

    @Test fun today_lowConfidence_dark_scale10() = captureToday(M15GoldenFixtures.todayLowConfidence(), true, 1f)

    @Test fun today_lowConfidence_dark_scale13() = captureToday(M15GoldenFixtures.todayLowConfidence(), true, 1.3f)

    @Test fun today_pregnancyWeek_light_scale10() = captureToday(M15GoldenFixtures.todayPregnancyWeek(), false, 1f)

    @Test fun today_pregnancyWeek_light_scale13() = captureToday(M15GoldenFixtures.todayPregnancyWeek(), false, 1.3f)

    @Test fun today_pregnancyWeek_dark_scale10() = captureToday(M15GoldenFixtures.todayPregnancyWeek(), true, 1f)

    @Test fun today_pregnancyWeek_dark_scale13() = captureToday(M15GoldenFixtures.todayPregnancyWeek(), true, 1.3f)

    @Test fun calendar_month_light_scale10() = captureCalendar(M15GoldenFixtures.calendarMonth(), false, 1f)

    @Test fun calendar_month_light_scale13() = captureCalendar(M15GoldenFixtures.calendarMonth(), false, 1.3f)

    @Test fun calendar_month_dark_scale10() = captureCalendar(M15GoldenFixtures.calendarMonth(), true, 1f)

    @Test fun calendar_month_dark_scale13() = captureCalendar(M15GoldenFixtures.calendarMonth(), true, 1.3f)

    @Test fun calendar_daySheet_light_scale10() = captureDaySheet(false, 1f)

    @Test fun calendar_daySheet_light_scale13() = captureDaySheet(false, 1.3f)

    @Test fun calendar_daySheet_dark_scale10() = captureDaySheet(true, 1f)

    @Test fun calendar_daySheet_dark_scale13() = captureDaySheet(true, 1.3f)

    private fun captureToday(
        state: dev.mahin.android.cycle.TodayUiState,
        darkTheme: Boolean,
        fontScale: Float,
    ) {
        composeRule.captureMahinFullScreenGolden(darkTheme, fontScale) {
            TodayScreenContent(state = state, modifier = Modifier.fillMaxSize())
        }
    }

    private fun captureCalendar(
        state: dev.mahin.android.cycle.CalendarUiState,
        darkTheme: Boolean,
        fontScale: Float,
    ) {
        composeRule.captureMahinFullScreenGolden(darkTheme, fontScale) {
            CycleCalendarScreenContent(
                state = state,
                onDateSelected = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    private fun captureDaySheet(
        darkTheme: Boolean,
        fontScale: Float,
    ) {
        val calendarState = M15GoldenFixtures.calendarDaySheetOpen()
        composeRule.captureMahinFullScreenGolden(darkTheme, fontScale) {
            SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                CycleCalendarScreenContent(
                    state = calendarState,
                    sharedTransitionScope = this,
                    onDateSelected = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
