package dev.mahin.backend.assistant

import org.springframework.stereotype.Component

@Component
class AssistantContextRedactor {
    fun redact(
        requestContext: AssistantTrackerContext?,
        consent: AssistantConsentScopes,
    ): String? {
        if (requestContext == null) return null
        val parts = mutableListOf<String>()
        if (consent.shareCycleSummary) {
            requestContext.cycleSummary?.takeIf { it.isNotBlank() }?.let { summary ->
                parts += "cycle_summary=${sanitize(summary)}"
            }
        }
        if (consent.shareSymptomTags && requestContext.symptomTags.isNotEmpty()) {
            val tags = requestContext.symptomTags.map { sanitize(it) }.joinToString(",")
            parts += "symptom_tags=$tags"
        }
        return parts.takeIf { it.isNotEmpty() }?.joinToString(";")
    }

    private fun sanitize(value: String): String =
        value
            .replace('\n', ' ')
            .trim()
            .take(MAX_FIELD_CHARS)

    companion object {
        private const val MAX_FIELD_CHARS = 120
    }
}
