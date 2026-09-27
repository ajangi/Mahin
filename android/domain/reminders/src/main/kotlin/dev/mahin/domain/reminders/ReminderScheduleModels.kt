package dev.mahin.domain.reminders

import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.cycle.CyclePredictionResult
import java.time.Instant
import java.time.LocalDate

data class PlannedReminder(
    val category: ReminderCategory,
    /** Stable WorkManager unique name suffix (no health payloads). */
    val stableKey: String,
    val triggerAt: Instant,
    val descriptiveFa: String?,
)

data class ReminderScheduleInput(
    val zoneId: String,
    val defaultLocalHour: Int = 9,
    val defaultLocalMinute: Int = 0,
    val now: Instant = Instant.now(),
    val periodUpcomingEnabled: Boolean = false,
    val periodLoggingFollowUpEnabled: Boolean = false,
    val ttcLoggingEnabled: Boolean = false,
    val pregnancyWeeklyEnabled: Boolean = false,
    val appointmentEnabled: Boolean = false,
)

data class AppointmentReminderSeed(
    val appointmentId: String,
    val scheduledAtEpochMs: Long,
    val reminderEnabled: Boolean,
)

data class ReminderPlanContext(
    val reproductiveMode: ReproductiveMode,
    val prediction: CyclePredictionResult?,
    val pregnancyLmpDate: LocalDate?,
    val activePregnancyId: String?,
    val appointments: List<AppointmentReminderSeed>,
    val openPeriodStart: LocalDate?,
    val onPeriodToday: Boolean,
)
