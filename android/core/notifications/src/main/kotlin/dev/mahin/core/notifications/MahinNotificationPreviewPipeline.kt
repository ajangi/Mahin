package dev.mahin.core.notifications

import dev.mahin.core.datastore.NotificationPrivacyMode
import dev.mahin.core.model.PregnancyOutcome

/**
 * Central gate for notification body text before scheduling or showing previews.
 *
 * **M5+ call site:** pregnancy-week / milestone WorkManager alarms must invoke
 * [resolveScheduledNotificationBody] with [PregnancyTrackingRepository.shouldSuppressCelebratoryNotifications]
 * (or equivalent) before posting. No scheduler exists in M4.
 */
object MahinNotificationPreviewPipeline {
    fun resolveScheduledNotificationBody(
        privacyMode: NotificationPrivacyMode,
        suppressCelebratoryFromStore: Boolean,
        latestOutcome: PregnancyOutcome?,
        descriptiveFa: String?,
    ): String {
        val suppress =
            suppressCelebratoryFromStore ||
                PregnancyNotificationSuppression.shouldSuppressCelebratoryPregnancyNotifications(
                    suppressFlag = false,
                    latestOutcome = latestOutcome,
                )
        if (suppress) {
            return DiscreetNotificationCopy.resolve(privacyMode, null)
        }
        val pregnancyPreview =
            PregnancyNotificationSuppression.resolvePregnancyWeeklyPreview(
                suppressFlag = false,
                latestOutcome = latestOutcome,
                descriptiveFa = descriptiveFa,
            )
        return DiscreetNotificationCopy.resolve(privacyMode, pregnancyPreview)
    }

    fun inAppCelebratoryWeekPreviewAllowed(
        suppressCelebratoryFromStore: Boolean,
        latestOutcome: PregnancyOutcome?,
    ): Boolean =
        PregnancyNotificationSuppression.celebratoryPreviewAllowed(
            suppressFlag = suppressCelebratoryFromStore,
            latestOutcome = latestOutcome,
        )
}
