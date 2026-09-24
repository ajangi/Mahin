package dev.mahin.core.analytics

import dev.mahin.core.common.SensitiveLogRedactor

/**
 * Privacy firewall for product analytics. Allowed event names are coarse product
 * funnels only; properties cannot carry raw health values.
 */
object SensitiveAnalyticsGuard {
    private val allowedEvents = setOf(
        "onboarding_started",
        "onboarding_completed",
        "calendar_opened",
        "log_category_opened",
        "log_saved",
        "prediction_explanation_opened",
        "pregnancy_week_opened",
        "appointment_created",
        "article_opened",
        "paywall_viewed",
        "subscription_started",
        "sync_failed",
        "foundation_opened",
    )

    fun sanitize(event: AnalyticsEvent): AnalyticsEvent {
        require(event.name in allowedEvents) {
            "Analytics event is not in the approved taxonomy"
        }
        event.properties.keys.forEach { SensitiveLogRedactor.assertSafeForTelemetry(it) }
        return event.copy(properties = SensitiveLogRedactor.sanitizeProperties(event.properties))
    }
}

class GuardedAnalyticsGateway(
    private val delegate: AnalyticsGateway,
) : AnalyticsGateway {
    override fun track(event: AnalyticsEvent) {
        delegate.track(SensitiveAnalyticsGuard.sanitize(event))
    }
}
