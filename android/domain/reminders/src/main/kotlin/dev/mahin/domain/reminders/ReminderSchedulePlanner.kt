package dev.mahin.domain.reminders

import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.cycle.CyclePredictionResult
import dev.mahin.domain.cycle.PredictionConfidence
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.min

/**
 * Pure Kotlin scheduling for local WorkManager alarms. Uses canonical instants;
 * Jalali is never persisted here.
 */
object ReminderSchedulePlanner {
    private const val PERIOD_UPCOMING_DAYS_BEFORE = 1L
    private const val PERIOD_FOLLOWUP_MAX_DAYS = 3L
    private const val APPOINTMENT_LEAD_HOURS = 24L
    private const val APPOINTMENT_SHORT_LEAD_MINUTES = 60L

    fun planAll(
        input: ReminderScheduleInput,
        context: ReminderPlanContext,
    ): List<PlannedReminder> {
        val zone = ReminderTimezone.resolveZone(input.zoneId)
        val today = LocalDate.ofInstant(input.now, zone)
        val plans = mutableListOf<PlannedReminder>()

        if (input.periodUpcomingEnabled && context.reproductiveMode != ReproductiveMode.PREGNANT) {
            planPeriodUpcoming(context.prediction, zone, input)?.let { plans += it }
        }
        if (input.periodLoggingFollowUpEnabled && context.reproductiveMode != ReproductiveMode.PREGNANT) {
            plans +=
                planPeriodLoggingFollowUp(
                    prediction = context.prediction,
                    zone = zone,
                    input = input,
                    today = today,
                    openPeriodStart = context.openPeriodStart,
                    onPeriodToday = context.onPeriodToday,
                )
        }
        if (input.ttcLoggingEnabled && context.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE) {
            planTtcWeeklyNudge(zone, input, today)?.let { plans += it }
        }
        if (input.pregnancyWeeklyEnabled && context.pregnancyLmpDate != null && context.activePregnancyId != null) {
            planPregnancyWeekly(
                pregnancyId = context.activePregnancyId,
                lmpDate = context.pregnancyLmpDate,
                zone = zone,
                input = input,
                today = today,
            )?.let { plans += it }
        }
        if (input.appointmentEnabled) {
            plans += planAppointments(context.appointments, zone, input)
        }
        return plans.sortedBy { it.triggerAt }
    }

    fun planPeriodUpcoming(
        prediction: CyclePredictionResult?,
        zone: ZoneId,
        input: ReminderScheduleInput,
    ): PlannedReminder? {
        if (prediction == null || prediction.confidence == PredictionConfidence.INSUFFICIENT_DATA) {
            return null
        }
        val next = prediction.nextPeriod ?: return null
        val triggerDate = next.earliest.minusDays(PERIOD_UPCOMING_DAYS_BEFORE)
        val trigger =
            ReminderTimezone.zonedTrigger(
                triggerDate,
                input.defaultLocalHour,
                input.defaultLocalMinute,
                zone,
            )
        if (!ReminderTimezone.isFuture(trigger, input.now)) return null
        return PlannedReminder(
            category = ReminderCategory.PERIOD_UPCOMING,
            stableKey = "period_upcoming_${next.earliest}",
            triggerAt = trigger.toInstant(),
            descriptiveFa = ReminderCategory.PERIOD_UPCOMING.defaultDescriptiveFa(),
        )
    }

    @Suppress("LongParameterList", "ReturnCount")
    fun planPeriodLoggingFollowUp(
        prediction: CyclePredictionResult?,
        zone: ZoneId,
        input: ReminderScheduleInput,
        today: LocalDate,
        openPeriodStart: LocalDate?,
        onPeriodToday: Boolean,
    ): List<PlannedReminder> {
        if (openPeriodStart != null || onPeriodToday) return emptyList()
        val earliest = prediction?.nextPeriod?.earliest ?: return emptyList()
        if (today.isBefore(earliest)) return emptyList()
        val daysSince = ChronoUnit.DAYS.between(earliest, today)
        if (daysSince >= PERIOD_FOLLOWUP_MAX_DAYS) return emptyList()
        val trigger =
            ReminderTimezone.zonedTrigger(
                today,
                input.defaultLocalHour,
                input.defaultLocalMinute,
                zone,
            )
        if (!ReminderTimezone.isFuture(trigger, input.now)) return emptyList()
        return listOf(
            PlannedReminder(
                category = ReminderCategory.PERIOD_LOGGING_FOLLOWUP,
                stableKey = "period_followup_$today",
                triggerAt = trigger.toInstant(),
                descriptiveFa = ReminderCategory.PERIOD_LOGGING_FOLLOWUP.defaultDescriptiveFa(),
            ),
        )
    }

    fun planTtcWeeklyNudge(
        zone: ZoneId,
        input: ReminderScheduleInput,
        today: LocalDate,
    ): PlannedReminder? {
        val trigger =
            ReminderTimezone.zonedTrigger(
                today,
                input.defaultLocalHour,
                input.defaultLocalMinute,
                zone,
            )
        if (!ReminderTimezone.isFuture(trigger, input.now)) return null
        return PlannedReminder(
            category = ReminderCategory.TTC_LOGGING,
            stableKey = "ttc_nudge_$today",
            triggerAt = trigger.toInstant(),
            descriptiveFa = ReminderCategory.TTC_LOGGING.defaultDescriptiveFa(),
        )
    }

    fun planPregnancyWeekly(
        pregnancyId: String,
        lmpDate: LocalDate,
        zone: ZoneId,
        input: ReminderScheduleInput,
        today: LocalDate,
    ): PlannedReminder? {
        val gestationalDays =
            ChronoUnit.DAYS
                .between(lmpDate, today)
                .toInt()
                .coerceAtLeast(0)
        val completedWeeks = gestationalDays / 7
        val nextWeekStart = lmpDate.plusDays(((completedWeeks + 1) * 7).toLong())
        if (nextWeekStart.isBefore(today)) return null
        val trigger =
            ReminderTimezone.zonedTrigger(
                nextWeekStart,
                input.defaultLocalHour,
                input.defaultLocalMinute,
                zone,
            )
        if (!ReminderTimezone.isFuture(trigger, input.now)) return null
        val weekNumber = completedWeeks + 1
        return PlannedReminder(
            category = ReminderCategory.PREGNANCY_WEEKLY,
            stableKey = "preg_week_${pregnancyId}_$weekNumber",
            triggerAt = trigger.toInstant(),
            descriptiveFa = ReminderCategory.PREGNANCY_WEEKLY.defaultDescriptiveFa(),
        )
    }

    fun planAppointments(
        appointments: List<AppointmentReminderSeed>,
        zone: ZoneId,
        input: ReminderScheduleInput,
    ): List<PlannedReminder> {
        val now = input.now
        return appointments
            .filter { it.reminderEnabled }
            .mapNotNull { seed ->
                val appointmentInstant = Instant.ofEpochMilli(seed.scheduledAtEpochMs)
                val minutesUntil = ChronoUnit.MINUTES.between(now, appointmentInstant)
                if (minutesUntil <= 0) return@mapNotNull null
                val triggerInstant =
                    if (minutesUntil >= APPOINTMENT_LEAD_HOURS * 60) {
                        appointmentInstant.minusSeconds(APPOINTMENT_LEAD_HOURS * 3600)
                    } else {
                        appointmentInstant.minusSeconds(APPOINTMENT_SHORT_LEAD_MINUTES * 60)
                    }
                if (!triggerInstant.isAfter(now)) return@mapNotNull null
                val triggerDate = triggerInstant.atZone(zone).toLocalDate()
                val aligned =
                    ReminderTimezone.zonedTrigger(
                        triggerDate,
                        min(input.defaultLocalHour, 23),
                        input.defaultLocalMinute,
                        zone,
                    )
                val fireAt =
                    if (aligned.toInstant().isAfter(now)) {
                        aligned.toInstant()
                    } else {
                        triggerInstant
                    }
                PlannedReminder(
                    category = ReminderCategory.APPOINTMENT,
                    stableKey = "appt_${seed.appointmentId}",
                    triggerAt = fireAt,
                    descriptiveFa = ReminderCategory.APPOINTMENT.defaultDescriptiveFa(),
                )
            }
    }

    fun workUniqueName(plan: PlannedReminder): String =
        "mahin_reminder_${plan.category.name.lowercase()}_${plan.stableKey}"
}
