package dev.mahin.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SensitiveLogRedactorTest {
    @Test
    fun dropsForbiddenHealthKeys() {
        val sanitized = SensitiveLogRedactor.sanitizeProperties(
            mapOf(
                "screen" to "today",
                "notes" to "cramps after period",
                "sexual_activity" to "unprotected",
                "period_start" to "2026-01-01",
            ),
        )
        assertThat(sanitized).containsExactly("screen", "today")
    }

    @Test
    fun redactValueNeverEchoesInput() {
        assertThat(SensitiveLogRedactor.redactValue("positive pregnancy test")).isEqualTo("[redacted]")
    }

    @Test(expected = IllegalStateException::class)
    fun assertSafeRejectsSymptomKey() {
        SensitiveLogRedactor.assertSafeForTelemetry("symptom_detail")
    }
}
