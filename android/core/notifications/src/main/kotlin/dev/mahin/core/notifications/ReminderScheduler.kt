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
    const val WORK_TAG = "mahin_reminder"
    const val MIN_DELAY = 15L
}

fun PlannedReminder.toInitialDelayMs(nowMs: Long = System.currentTimeMillis()): Long {
    val delay = triggerAt.toEpochMilli() - nowMs
    return delay.coerceAtLeast(ReminderWorkerKeys.MIN_DELAY * 60 * 1000)
}

fun PlannedReminder.toWorkDelay(): Duration = Duration.ofMillis(toInitialDelayMs())
