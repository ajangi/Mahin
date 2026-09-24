package dev.mahin.core.datetime

import java.time.LocalDate

/**
 * Conversion boundary implemented in M1. Domain code must depend on [java.time.LocalDate]
 * / Instant, not on this converter.
 */
interface CivilDateConverter {
    fun toJalali(isoDate: LocalDate): JalaliDate
    fun toGregorian(jalaliDate: JalaliDate): LocalDate
}
