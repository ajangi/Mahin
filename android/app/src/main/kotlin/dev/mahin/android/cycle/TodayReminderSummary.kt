package dev.mahin.android.cycle

import dev.mahin.core.database.cycle.CycleDashboard
import dev.mahin.core.database.entity.PregnancyRecordEntity
import dev.mahin.core.datastore.NotificationPreferenceSnapshot
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.reminders.AppointmentReminderSeed
import dev.mahin.domain.reminders.ReminderCategory
import dev.mahin.domain.reminders.ReminderPlanContext
import dev.mahin.domain.reminders.ReminderScheduleInput
import dev.mahin.domain.reminders.ReminderSchedulePlanner
import java.time.Instant

data class TodayUpcomingReminder(
    val descriptiveFa: String,
    val triggerAtEpochMs: Long,
)

/** Next pregnancy plan appointment (opens Plan tab). */
data class TodayUpcomingAppointment(
    val titleFa: String,
    val scheduledAtEpochMs: Long,
)

internal object TodayReminderSummary {
    @Suppress("LongParameterList")
    fun nextUpcoming(
        snapshot: NotificationPreferenceSnapshot,
        dashboard: CycleDashboard,
        reproductiveMode: ReproductiveMode,
        pregnancy: PregnancyRecordEntity?,
        appointments: List<AppointmentReminderSeed>,
        now: Instant = Instant.now(),
    ): TodayUpcomingReminder? {
        val input =
            ReminderScheduleInput(
                zoneId = snapshot.zoneId,
                defaultLocalHour = snapshot.localHour,
                defaultLocalMinute = snapshot.localMinute,
                now = now,
                periodUpcomingEnabled =
                    snapshot.categoryEnabled[ReminderCategory.PERIOD_UPCOMING] == true,
                periodLoggingFollowUpEnabled =
                    snapshot.categoryEnabled[ReminderCategory.PERIOD_LOGGING_FOLLOWUP] == true,
                ttcLoggingEnabled = snapshot.categoryEnabled[ReminderCategory.TTC_LOGGING] == true,
                pregnancyWeeklyEnabled =
                    snapshot.categoryEnabled[ReminderCategory.PREGNANCY_WEEKLY] == true,
                appointmentEnabled = snapshot.categoryEnabled[ReminderCategory.APPOINTMENT] == true,
            )
        val plans =
            ReminderSchedulePlanner.planAll(
                input = input,
                context =
                    ReminderPlanContext(
                        reproductiveMode = reproductiveMode,
                        prediction = dashboard.prediction,
                        pregnancyLmpDate = pregnancy?.lmpDate,
                        activePregnancyId = pregnancy?.id,
                        appointments = appointments,
                        openPeriodStart = dashboard.openPeriodStart,
                        onPeriodToday = dashboard.onPeriodToday,
                    ),
            )
        val next = plans.firstOrNull() ?: return null
        val label = next.descriptiveFa ?: return null
        return TodayUpcomingReminder(
            descriptiveFa = label,
            triggerAtEpochMs = next.triggerAt.toEpochMilli(),
        )
    }
}
