package dev.mahin.core.datetime

import java.time.LocalDate

/**
 * Jalali (Persian) civil calendar conversion. Algorithm follows the widely used
 * 33-year leap cycle implementation (see ADR 0008 — canonical storage remains Gregorian).
 */
internal object JalaliCalendarMath {
    fun toJalali(gregorian: LocalDate): JalaliDate {
        val (jy, jm, jd) = gregorianToJalali(gregorian.year, gregorian.monthValue, gregorian.dayOfMonth)
        return JalaliDate(year = jy, month = jm, day = jd)
    }

    fun toGregorian(jalali: JalaliDate): LocalDate {
        val (gy, gm, gd) = jalaliToGregorian(jalali.year, jalali.month, jalali.day)
        return LocalDate.of(gy, gm, gd)
    }

    fun daysInJalaliMonth(
        year: Int,
        month: Int,
    ): Int {
        require(month in 1..12)
        if (month <= 6) return 31
        if (month <= 11) return 30
        return if (isJalaliLeapYear(year)) 30 else 29
    }

    fun isJalaliLeapYear(year: Int): Boolean {
        val nextYearStart = toGregorian(JalaliDate(year = year + 1, month = 1, day = 1))
        val thisYearStart = toGregorian(JalaliDate(year = year, month = 1, day = 1))
        return nextYearStart.toEpochDay() - thisYearStart.toEpochDay() == 366L
    }

    private fun gregorianToJalali(
        gYear: Int,
        gMonth: Int,
        gDay: Int,
    ): Triple<Int, Int, Int> {
        var gy = gYear - 1600
        var gm = gMonth - 1
        val gd = gDay - 1

        var gDayNo =
            365 * gy +
                (gy + 3) / 4 -
                (gy + 99) / 100 +
                (gy + 399) / 400
        val gregorianMonthLengths =
            intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        if (isGregorianLeapYear(gYear)) {
            gregorianMonthLengths[2] = 29
        }
        for (i in 0 until gm) {
            gDayNo += gregorianMonthLengths[i + 1]
        }
        gDayNo += gd

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053
        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        val jm: Int
        val jd: Int
        if (jDayNo < 186) {
            jm = 1 + jDayNo / 31
            jd = 1 + jDayNo % 31
        } else {
            jm = 7 + (jDayNo - 186) / 30
            jd = 1 + (jDayNo - 186) % 30
        }
        return Triple(jy, jm, jd)
    }

    private fun jalaliToGregorian(
        jYear: Int,
        jMonth: Int,
        jDay: Int,
    ): Triple<Int, Int, Int> {
        var jy = jYear - 979
        var jm = jMonth - 1
        val jd = jDay - 1

        var jDayNo = 365 * jy + jy / 33 * 8 + (jy % 33 + 3) / 4
        for (i in 0 until jm) {
            jDayNo += if (i < 6) 31 else 30
        }
        jDayNo += jd

        var gDayNo = jDayNo + 79
        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524
            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }

        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }

        val gregorianMonthLengths =
            intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        if (leap) {
            gregorianMonthLengths[2] = 29
        }

        var gm = 0
        while (gm < 13 && gDayNo >= gregorianMonthLengths[gm + 1]) {
            gDayNo -= gregorianMonthLengths[gm + 1]
            gm++
        }
        val gd = gDayNo + 1
        return Triple(gy, gm + 1, gd)
    }

    private fun isGregorianLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
}
