package dev.mahin.core.notifications

import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.NotificationPreferencesRepository
import dev.mahin.core.datastore.NotificationPrivacyMode
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.reminders.AppointmentReminderSeed
import dev.mahin.domain.reminders.ReminderCategory
import dev.mahin.domain.reminders.ReminderPlanContext
import dev.mahin.domain.reminders.ReminderScheduleInput
import dev.mahin.domain.reminders.ReminderSchedulePlanner
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class ReminderPlanner
    @Inject
    constructor(
        private val preferencesRepository: NotificationPreferencesRepository,
        private val cycleRepository: CycleTrackingRepository,
        private val pregnancyRepository: PregnancyTrackingRepository,
        private val scheduler: ReminderScheduler,
    ) {
        suspend fun refreshAllPlans() {
            val snapshot = preferencesRepository.observeSnapshot().first()
            if (snapshot.privacyMode == NotificationPrivacyMode.OFF) {
                scheduler.cancelAllMahinReminders()
                return
            }
            val anyCategoryEnabled = snapshot.categoryEnabled.values.any { it }
            if (!anyCategoryEnabled) {
                scheduler.cancelAllMahinReminders()
                return
            }
            val today = LocalDate.now()
            val dashboard = cycleRepository.observeDashboard(today).first()
            val profile = dashboard.profile
            val reproductiveMode = profile?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING
            val pregnancy = pregnancyRepository.getActivePregnancy()
            val appointments =
                pregnancy?.let { active ->
                    pregnancyRepository
                        .upcomingAppointments(
                            pregnancyId = active.id,
                            fromEpochMs = System.currentTimeMillis(),
                            limit = 32,
                        ).map { entity ->
                            AppointmentReminderSeed(
                                appointmentId = entity.id,
                                scheduledAtEpochMs = entity.scheduledAtEpochMs,
                                reminderEnabled = entity.reminderEnabled,
                                descriptiveFa = ReminderCategorySafeCopy.appointment(entity.title),
                            )
                        }
                } ?: emptyList()
            val input =
                ReminderScheduleInput(
                    zoneId = snapshot.zoneId,
                    defaultLocalHour = snapshot.localHour,
                    defaultLocalMinute = snapshot.localMinute,
                    periodUpcomingEnabled = snapshot.categoryEnabled[ReminderCategory.PERIOD_UPCOMING] == true,
                    periodLoggingFollowUpEnabled =
                        snapshot.categoryEnabled[ReminderCategory.PERIOD_LOGGING_FOLLOWUP] == true,
                    ttcLoggingEnabled = snapshot.categoryEnabled[ReminderCategory.TTC_LOGGING] == true,
                    pregnancyWeeklyEnabled = snapshot.categoryEnabled[ReminderCategory.PREGNANCY_WEEKLY] == true,
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
            scheduler.applyPlans(plans)
        }
    }

private object ReminderCategorySafeCopy {
    fun appointment(title: String): String = title.trim().ifEmpty { "یادآوری قرار" }
}
