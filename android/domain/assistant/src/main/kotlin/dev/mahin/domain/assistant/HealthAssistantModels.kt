package dev.mahin.domain.assistant

data class AssistantConsentScopes(
    val shareCycleSummary: Boolean = false,
    val shareSymptomTags: Boolean = false,
)

data class AssistantConsentState(
    val scopes: AssistantConsentScopes = AssistantConsentScopes(),
    val updatedAtEpochMs: Long? = null,
)

enum class AssistantOutcome {
    ANSWERED,
    ESCALATED,
    REFUSED,
    DISABLED,
    NOT_CONFIGURED,
    ERROR,
}

data class ContentCitation(
    val documentId: String,
    val versionId: String,
    val title: String,
    val slug: String,
)

data class AssistantAnswer(
    val outcome: AssistantOutcome,
    val answer: String? = null,
    val citations: List<ContentCitation> = emptyList(),
    val escalationCode: String? = null,
    val refusalCode: String? = null,
    val disclaimer: String? = null,
)

data class AssistantAskInput(
    val locale: String = "fa-IR",
    val question: String,
    val cycleSummary: String? = null,
    val symptomTags: List<String> = emptyList(),
)
