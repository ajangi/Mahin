package dev.mahin.domain.reminders

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object ReminderTimezone {
    fun resolveZone(zoneId: String): ZoneId = runCatching { ZoneId.of(zoneId) }.getOrElse { ZoneId.systemDefault() }

    fun zonedTrigger(
        date: LocalDate,
        hour: Int,
        minute: Int,
        zone: ZoneId,
    ): ZonedDateTime = ZonedDateTime.of(date, LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59)), zone)

    fun isFuture(
        trigger: ZonedDateTime,
        now: Instant,
    ): Boolean = trigger.toInstant().isAfter(now)
}
