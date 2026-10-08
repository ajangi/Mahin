package dev.mahin.android.cycle

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import java.time.LocalDate
import org.junit.Test

class CalendarJumpToTodayTest {
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
}
