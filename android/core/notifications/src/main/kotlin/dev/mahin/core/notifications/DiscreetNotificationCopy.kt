package dev.mahin.core.notifications

import dev.mahin.core.datastore.NotificationPrivacyMode

/**
 * Default lock-screen copy. Must not mention menstruation, pregnancy, sexual
 * activity, medications, or test results.
 */
object DiscreetNotificationCopy {
    const val DEFAULT_FA: String = "یادآوری شما آماده است"

    fun resolve(mode: NotificationPrivacyMode, descriptiveFa: String?): String {
        return when (mode) {
            NotificationPrivacyMode.OFF -> ""
            NotificationPrivacyMode.DISCREET -> DEFAULT_FA
            NotificationPrivacyMode.DESCRIPTIVE -> descriptiveFa?.takeIf { it.isNotBlank() } ?: DEFAULT_FA
        }
    }

    fun containsSensitiveLeak(text: String): Boolean {
        val normalized = text.lowercase()
        val forbidden = listOf(
            "پریود",
            "باردار",
            "تخمک",
            "نزدیکی",
            "بی‌بی‌چک",
            "period",
            "pregnan",
            "ovulat",
            "sex",
        )
        return forbidden.any { normalized.contains(it) }
    }
}

interface NotificationGateway {
    fun canPost(mode: NotificationPrivacyMode): Boolean
}
