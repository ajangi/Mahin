@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package dev.mahin.android.cycle

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.datetime.PersianDigits
import dev.mahin.core.designsystem.MahinTheme
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], qualifiers = "fa-rIR", manifest = Config.NONE)
class CalendarJumpToTodayTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun applyJumpToToday_setsSelectedAndVisibleMonth() {
        val todayJalali = PersianCivilDateConverter.toJalali(LocalDate.now())
        val prior =
            CalendarUiState(
                selectedJalali = JalaliDate(1400, 1, 1),
                visibleMonth = JalaliDate(1400, 1, 1),
            )
        val updated =
            CalendarViewModel.applyJumpToToday(
                state = prior,
                todayJalali = todayJalali,
            )
        assertThat(updated.selectedJalali).isEqualTo(todayJalali)
        assertThat(updated.visibleMonth).isEqualTo(JalaliDate(todayJalali.year, todayJalali.month, 1))
    }

    @Test
    fun jumpToToday_fromFutureMonth_showsCurrentMonthInHeader() {
        val todayJalali = PersianCivilDateConverter.toJalali(LocalDate.of(2025, 3, 14))
        val monthNames =
            arrayOf(
                "فروردین",
                "اردیبهشت",
                "خرداد",
                "تیر",
                "مرداد",
                "شهریور",
                "مهر",
                "آبان",
                "آذر",
                "دی",
                "بهمن",
                "اسفند",
            )
        val expectedHeader = "${monthNames[todayJalali.month - 1]} ${PersianDigits.format(todayJalali.year)}"
        var state by mutableStateOf(
            CalendarUiState(
                selectedJalali = JalaliDate(1400, 1, 1),
                visibleMonth = JalaliDate(1400, 1, 1),
            ),
        )
        composeRule.setContent {
            MahinTheme {
                SharedTransitionLayout {
                    CycleCalendarScreenContent(
                        state = state,
                        sharedTransitionScope = this,
                        onDateSelected = {},
                        onDismissDaySheet = {},
                        onToggleLegend = {},
                        onJumpToToday = {
                            state =
                                CalendarViewModel.applyJumpToToday(
                                    state,
                                    todayJalali,
                                )
                        },
                        onVisibleMonthChanged = { month ->
                            state = state.copy(visibleMonth = month)
                        },
                    )
                }
            }
        }
        composeRule.onNodeWithContentDescription("ماه بعد").performClick()
        composeRule.onNodeWithText("برو به امروز").performClick()
        composeRule.onNodeWithTag("jalali_month_header_label").assertTextEquals(expectedHeader)
    }
}
