package dev.mahin.core.common

/**
 * Redacts values that must never appear in logs, crash metadata, analytics, or traces.
 * This is a last-line guard; callers must still avoid putting health data in log sites.
 */
object SensitiveLogRedactor {
    private val forbiddenKeys = setOf(
        "note",
        "notes",
        "free_text",
        "symptom",
        "symptoms",
        "sexual_activity",
        "intercourse",
        "pregnancy_test",
        "period_start",
        "period_end",
        "cycle_start",
        "lmp",
        "edd",
        "bbt",
        "medication",
        "password",
        "token",
        "refresh_token",
        "authorization",
        "phone",
        "email",
    )

    private val forbiddenSubstrings = listOf(
        "period",
        "pregnan",
        "ovulat",
        "fertile",
        "sperm",
        "intercourse",
        "symptom",
        "lmp",
        "due date",
        "kick",
        "contraction",
    )

    fun isForbiddenKey(key: String): Boolean {
        val normalized = normalize(key)
        if (normalized in forbiddenKeys) return true
        return forbiddenSubstrings.any { normalized.contains(it) }
    }

    fun redactValue(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        return "[redacted]"
    }

    fun sanitizeProperties(input: Map<String, String?>): Map<String, String> {
        return input.mapNotNull { (key, value) ->
            if (isForbiddenKey(key)) {
                null
            } else {
                key to (value?.take(64) ?: "")
            }
        }.toMap()
    }

    fun assertSafeForTelemetry(key: String) {
        check(!isForbiddenKey(key)) {
            "Refusing to emit sensitive telemetry key"
        }
    }

    private fun normalize(key: String): String =
        key.trim().lowercase().replace('-', '_').replace(' ', '_')
}
