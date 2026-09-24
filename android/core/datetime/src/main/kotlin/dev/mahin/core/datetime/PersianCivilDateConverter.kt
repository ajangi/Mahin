package dev.mahin.core.datetime

import java.time.LocalDate

/** Default [CivilDateConverter] for Mahin (Jalali presentation, Gregorian persistence). */
object PersianCivilDateConverter : CivilDateConverter {
    override fun toJalali(isoDate: LocalDate): JalaliDate = JalaliCalendarMath.toJalali(isoDate)

    override fun toGregorian(jalaliDate: JalaliDate): LocalDate = JalaliCalendarMath.toGregorian(jalaliDate)
}
