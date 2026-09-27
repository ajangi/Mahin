package dev.mahin.core.notifications

import dev.mahin.core.datastore.NotificationPrivacyMode
import dev.mahin.core.model.PregnancyOutcome
import dev.mahin.domain.reminders.ReminderCategory

data class ReminderNotificationRequest(
    val notificationId: Int,
    val privacyMode: NotificationPrivacyMode,
    val suppressCelebratory: Boolean,
    val latestOutcome: PregnancyOutcome?,
    val category: ReminderCategory,
    val descriptiveFa: String?,
)
