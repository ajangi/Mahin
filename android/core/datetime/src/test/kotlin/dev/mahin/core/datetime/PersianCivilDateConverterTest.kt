package dev.mahin.core.datetime

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class PersianCivilDateConverterTest {
    private val converter: CivilDateConverter = PersianCivilDateConverter

    @Test
    fun nowruz1403MapsToGregorian() {
        val jalali = JalaliDate(year = 1403, month = 1, day = 1)
        assertThat(converter.toGregorian(jalali)).isEqualTo(LocalDate.of(2024, 3, 20))
        assertThat(converter.toJalali(LocalDate.of(2024, 3, 20))).isEqualTo(jalali)
    }

    @Test
    fun roundTripAcrossLeapEsfand() {
        val jalali = JalaliDate(year = 1403, month = 12, day = 30)
        val gregorian = converter.toGregorian(jalali)
        assertThat(converter.toJalali(gregorian)).isEqualTo(jalali)
    }

    @Test
    fun nonLeapEsfandHasTwentyNineDays() {
        val jalali = JalaliDate(year = 1402, month = 12, day = 29)
        val gregorian = converter.toGregorian(jalali)
        assertThat(converter.toJalali(gregorian)).isEqualTo(jalali)
        assertThat(JalaliCalendar.daysInMonth(1402, 12)).isEqualTo(29)
    }

    @Test
    fun yearBoundaryGregorianToJalali() {
        assertThat(converter.toJalali(LocalDate.of(2025, 12, 31)))
            .isEqualTo(JalaliDate(year = 1404, month = 10, day = 10))
    }

    @Test
    fun persianDigitsFormatAsciiNumbers() {
        assertThat(PersianDigits.format(1403)).isEqualTo("۱۴۰۳")
        assertThat(PersianDigits.format("28")).isEqualTo("۲۸")
    }
}
