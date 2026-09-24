package dev.mahin.core.datetime

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Canonical persistence policy for Mahin dates.
 *
 * - Calendar-day facts (period start, LMP, EDD as a date) persist as ISO-8601 [LocalDate]
 *   in the Gregorian calendar.
 * - Instants (createdAt, sync, contractions) persist as UTC [Instant].
 * - Jalali is a presentation/input conversion only.
 */
object CanonicalDatePolicy {
    fun requirePersistedLocalDate(date: LocalDate): String = date.toString()

    fun parsePersistedLocalDate(raw: String): LocalDate = LocalDate.parse(raw)

    fun requireUtcInstant(instant: Instant): String = instant.toString()

    fun today(zoneId: ZoneId): LocalDate = LocalDate.now(zoneId)

    fun utc(): ZoneId = ZoneOffset.UTC
}
