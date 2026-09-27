package dev.mahin.domain.reminders

/**
 * Independently configurable reminder families (PRD §12).
 * Copy resolved at display time via [dev.mahin.core.notifications.DiscreetNotificationCopy].
 */
enum class ReminderCategory {
    PERIOD_UPCOMING,
    PERIOD_LOGGING_FOLLOWUP,
    TTC_LOGGING,
    PREGNANCY_WEEKLY,
    APPOINTMENT,
    ;

    fun defaultDescriptiveFa(): String =
        when (this) {
            PERIOD_UPCOMING -> "یادآوری رویداد پیش‌بینی‌شده"
            PERIOD_LOGGING_FOLLOWUP -> "یادآوری ثبت روزانه"
            TTC_LOGGING -> "یادآوری ثبت"
            PREGNANCY_WEEKLY -> "یادآوری محتوای هفتگی"
            APPOINTMENT -> "یادآوری قرار"
        }
}
