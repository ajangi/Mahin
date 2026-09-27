package dev.mahin.core.notifications

import dev.mahin.domain.reminders.PlannedReminder
import java.time.Duration

interface ReminderScheduler {
    suspend fun applyPlans(plans: List<PlannedReminder>)

    suspend fun cancelAllMahinReminders()

    fun requestFullRefresh()
}

class WorkManagerReminderScheduler(
    private val delegate: ReminderWorkScheduler,
) : ReminderScheduler {
    override suspend fun applyPlans(plans: List<PlannedReminder>) {
        delegate.applyPlans(plans)
    }

    override suspend fun cancelAllMahinReminders() {
        delegate.cancelAllMahinReminders()
    }

    override fun requestFullRefresh() {
        delegate.enqueueRefresh()
    }
}

interface ReminderWorkScheduler {
    suspend fun applyPlans(plans: List<PlannedReminder>)

    suspend fun cancelAllMahinReminders()

    fun enqueueRefresh()
}

object ReminderWorkerKeys {
    const val CATEGORY = "category"
    const val STABLE_KEY = "stable_key"
    const val REFRESH_WORK = "mahin_reminder_refresh"
    const val PERIODIC_REPLAN = "mahin_reminder_periodic_replan"

    /** One-shot [ReminderWorker] alarms only. Never used for refresh/replan workers. */
    const val FIRE_TAG = "mahin_reminder_fire"

    /** [ReminderRefreshWorker] one-shot and 12h periodic replan. */
    const val REFRESH_TAG = "mahin_reminder_refresh_run"

    /**
     * WorkManager may enforce a minimum delay (~15 minutes) on one-time work; we do not
     * artificially extend delay beyond [PlannedReminder.triggerAt] (see [toInitialDelayMs]).
     */
    const val DOCUMENTED_WM_MIN_DELAY_MINUTES = 15L
}

fun PlannedReminder.toInitialDelayMs(nowMs: Long = System.currentTimeMillis()): Long {
    val raw = triggerAt.toEpochMilli() - nowMs
    return raw.coerceAtLeast(0L)
}

fun PlannedReminder.toWorkDelay(): Duration = Duration.ofMillis(toInitialDelayMs())
