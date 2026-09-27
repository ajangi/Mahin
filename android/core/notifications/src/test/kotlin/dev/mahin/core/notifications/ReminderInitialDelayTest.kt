package dev.mahin.core.notifications

import dev.mahin.domain.reminders.PlannedReminder
import dev.mahin.domain.reminders.ReminderCategory
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderInitialDelayTest {
    @Test
    fun toInitialDelayMs_doesNotApplyArtificialFloorBeforeTrigger() {
        val now = Instant.parse("2026-03-10T08:00:00Z")
        val plan =
            PlannedReminder(
                category = ReminderCategory.APPOINTMENT,
                stableKey = "appt_1",
                triggerAt = Instant.parse("2026-03-10T08:05:00Z"),
                descriptiveFa = null,
            )
        assertEquals(5 * 60 * 1000L, plan.toInitialDelayMs(now.toEpochMilli()))
    }
}
