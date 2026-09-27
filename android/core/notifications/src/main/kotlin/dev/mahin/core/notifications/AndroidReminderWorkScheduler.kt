package dev.mahin.core.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.mahin.domain.reminders.PlannedReminder
import dev.mahin.domain.reminders.ReminderSchedulePlanner
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidReminderWorkScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : ReminderWorkScheduler {
        private val workManager get() = WorkManager.getInstance(context)

        override suspend fun applyPlans(plans: List<PlannedReminder>) {
            cancelAllMahinReminders()
            plans.forEach { plan ->
                val delayMs = plan.toInitialDelayMs()
                val request =
                    OneTimeWorkRequestBuilder<ReminderWorker>()
                        .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                        .addTag(ReminderWorkerKeys.WORK_TAG)
                        .setInputData(
                            workDataOf(
                                ReminderWorkerKeys.CATEGORY to plan.category.name,
                                ReminderWorkerKeys.STABLE_KEY to plan.stableKey,
                            ),
                        ).build()
                workManager.enqueueUniqueWork(
                    ReminderSchedulePlanner.workUniqueName(plan),
                    ExistingWorkPolicy.REPLACE,
                    request,
                )
            }
        }

        override suspend fun cancelAllMahinReminders() {
            workManager.cancelAllWorkByTag(ReminderWorkerKeys.WORK_TAG)
        }

        override fun enqueueRefresh() {
            val refresh =
                OneTimeWorkRequestBuilder<ReminderRefreshWorker>()
                    .addTag(ReminderWorkerKeys.WORK_TAG)
                    .build()
            workManager.enqueueUniqueWork(
                ReminderWorkerKeys.REFRESH_WORK,
                ExistingWorkPolicy.KEEP,
                refresh,
            )
        }

        fun ensurePeriodicReplan() {
            val periodic =
                PeriodicWorkRequestBuilder<ReminderRefreshWorker>(12, TimeUnit.HOURS)
                    .addTag(ReminderWorkerKeys.WORK_TAG)
                    .build()
            workManager.enqueueUniquePeriodicWork(
                ReminderWorkerKeys.PERIODIC_REPLAN,
                ExistingPeriodicWorkPolicy.KEEP,
                periodic,
            )
        }
    }
