package dev.mahin.backend.assistant

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AssistantContextRedactorTest {
    private val redactor = AssistantContextRedactor()

    @Test
    fun withoutConsentRedactsAll() {
        val result =
            redactor.redact(
                AssistantTrackerContext(
                    cycleSummary = "SECRET_CYCLE_SUMMARY",
                    symptomTags = listOf("SECRET_TAG"),
                ),
                AssistantConsentScopes(),
            )
        assertNull(result)
    }

    @Test
    fun cycleSummaryOnlyWhenConsented() {
        val result =
            redactor.redact(
                AssistantTrackerContext(
                    cycleSummary = "SECRET_CYCLE_SUMMARY",
                    symptomTags = listOf("SECRET_TAG"),
                ),
                AssistantConsentScopes(shareCycleSummary = true),
            )
        assertNotNull(result)
        assertTrue(result!!.contains("cycle_summary"))
        assertFalse(result.contains("SECRET_TAG"))
        assertFalse(result.contains("symptom_tags"))
    }

    @Test
    fun symptomTagsOnlyWhenConsented() {
        val result =
            redactor.redact(
                AssistantTrackerContext(
                    cycleSummary = "SECRET_CYCLE_SUMMARY",
                    symptomTags = listOf("SECRET_TAG"),
                ),
                AssistantConsentScopes(shareSymptomTags = true),
            )
        assertNotNull(result)
        assertTrue(result!!.contains("symptom_tags"))
        assertFalse(result.contains("SECRET_CYCLE_SUMMARY"))
    }

    @Test
    fun bothScopesWhenConsented() {
        val result =
            redactor.redact(
                AssistantTrackerContext(
                    cycleSummary = "SECRET_CYCLE_SUMMARY",
                    symptomTags = listOf("SECRET_TAG"),
                ),
                AssistantConsentScopes(shareCycleSummary = true, shareSymptomTags = true),
            )
        assertNotNull(result)
        assertTrue(result!!.contains("cycle_summary"))
        assertTrue(result.contains("symptom_tags"))
    }

    @Test
    fun truncatesLongFields() {
        val longSummary = "x".repeat(200)
        val result =
            redactor.redact(
                AssistantTrackerContext(cycleSummary = longSummary),
                AssistantConsentScopes(shareCycleSummary = true),
            )
        assertNotNull(result)
        assertEquals(120, result!!.substringAfter("cycle_summary=").length)
    }
}
