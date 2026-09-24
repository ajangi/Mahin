package dev.mahin.core.datetime

/** Public helpers for Jalali calendar UI (conversion policy remains ADR 0008). */
object JalaliCalendar {
    fun daysInMonth(
        year: Int,
        month: Int,
    ): Int = JalaliCalendarMath.daysInJalaliMonth(year, month)

    fun isLeapYear(year: Int): Boolean = JalaliCalendarMath.isJalaliLeapYear(year)
}
