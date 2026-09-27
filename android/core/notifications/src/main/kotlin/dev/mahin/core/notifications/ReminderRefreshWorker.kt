package dev.mahin.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderRefreshWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted params: WorkerParameters,
        private val planner: ReminderPlanner,
        private val workScheduler: AndroidReminderWorkScheduler,
    ) : CoroutineWorker(appContext, params) {
        override suspend fun doWork(): Result {
            workScheduler.ensurePeriodicReplan()
            planner.refreshAllPlans()
            return Result.success()
        }
    }
