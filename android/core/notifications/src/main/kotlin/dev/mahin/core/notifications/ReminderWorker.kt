package dev.mahin.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.NotificationPreferencesRepository
import dev.mahin.domain.reminders.ReminderCategory
import kotlinx.coroutines.flow.first

@HiltWorker
class ReminderWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted params: WorkerParameters,
        private val presenter: ReminderNotificationPresenter,
        private val preferencesRepository: NotificationPreferencesRepository,
        private val pregnancyRepository: PregnancyTrackingRepository,
    ) : CoroutineWorker(appContext, params) {
        override suspend fun doWork(): Result {
            val categoryRaw = inputData.getString(ReminderWorkerKeys.CATEGORY) ?: return Result.failure()
            val category =
                runCatching { ReminderCategory.valueOf(categoryRaw) }.getOrElse { return Result.failure() }
            val stableKey = inputData.getString(ReminderWorkerKeys.STABLE_KEY) ?: return Result.failure()
            val prefs = preferencesRepository.observeSnapshot().first()
            val suppress = pregnancyRepository.shouldSuppressCelebratoryNotifications()
            val pregnancy = pregnancyRepository.getActivePregnancy()
            presenter.showReminder(
                ReminderNotificationRequest(
                    notificationId = stableKey.hashCode(),
                    privacyMode = prefs.privacyMode,
                    suppressCelebratory = suppress,
                    latestOutcome = pregnancy?.outcome,
                    category = category,
                    descriptiveFa = category.defaultDescriptiveFa(),
                ),
            )
            return Result.success()
        }
    }
