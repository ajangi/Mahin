package dev.mahin.core.analytics

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SensitiveAnalyticsGuardTest {
    @Test
    fun acceptsApprovedEventWithSafeProps() {
        val event =
            SensitiveAnalyticsGuard.sanitize(
                AnalyticsEvent("onboarding_completed", mapOf("mode" to "CYCLE_TRACKING")),
            )
        assertThat(event.properties).containsEntry("mode", "CYCLE_TRACKING")
    }

    @Test(expected = IllegalStateException::class)
    fun rejectsHealthProperty() {
        SensitiveAnalyticsGuard.sanitize(
            AnalyticsEvent("log_saved", mapOf("notes" to "secret")),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnknownEventName() {
        SensitiveAnalyticsGuard.sanitize(AnalyticsEvent("period_started_today"))
    }
}
