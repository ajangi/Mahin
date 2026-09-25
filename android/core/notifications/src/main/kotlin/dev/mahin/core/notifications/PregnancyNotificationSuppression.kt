package dev.mahin.core.notifications

import dev.mahin.core.model.PregnancyOutcome

/**
 * Blocks celebratory or pregnancy-specific notification copy after sensitive outcomes.
 */
object PregnancyNotificationSuppression {
    fun shouldSuppressCelebratoryPregnancyNotifications(
        suppressFlag: Boolean,
        latestOutcome: PregnancyOutcome?,
    ): Boolean {
        if (suppressFlag) return true
        return when (latestOutcome) {
            PregnancyOutcome.PREGNANCY_LOSS,
            PregnancyOutcome.TERMINATION,
            -> true
            else -> false
        }
    }

    fun celebratoryPreviewAllowed(
        suppressFlag: Boolean,
        latestOutcome: PregnancyOutcome?,
    ): Boolean = !shouldSuppressCelebratoryPregnancyNotifications(suppressFlag, latestOutcome)

    fun resolvePregnancyWeeklyPreview(
        suppressFlag: Boolean,
        latestOutcome: PregnancyOutcome?,
        descriptiveFa: String?,
    ): String? {
        if (!celebratoryPreviewAllowed(suppressFlag, latestOutcome)) return null
        return descriptiveFa?.takeIf { !DiscreetNotificationCopy.containsSensitiveLeak(it) }
    }
}
