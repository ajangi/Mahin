package dev.mahin.backend.assistant

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class AssistantConsentScopes(
    val shareCycleSummary: Boolean = false,
    val shareSymptomTags: Boolean = false,
)

data class AssistantConsentResponse(
    val scopes: AssistantConsentScopes,
    val updatedAtEpochMs: Long?,
)

data class UpdateAssistantConsentRequest(
    @field:Valid val scopes: AssistantConsentScopes,
)

data class AssistantTrackerContext(
    @field:Size(max = 512) val cycleSummary: String? = null,
    val symptomTags: List<
        @Size(max = 64)
        String,
    > = emptyList(),
)

data class AssistantAskRequest(
    @field:Size(max = 16) val locale: String = "fa-IR",
    @field:NotBlank @field:Size(max = 2000) val question: String,
    @field:Valid val trackerContext: AssistantTrackerContext? = null,
)

enum class AssistantOutcomeClass {
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

data class AssistantAskResponse(
    val outcome: AssistantOutcomeClass,
    val answer: String? = null,
    val citations: List<ContentCitation> = emptyList(),
    val escalationCode: String? = null,
    val refusalCode: String? = null,
    val disclaimer: String =
        "این پاسخ صرفاً آموزشی است و جایگزین مشاورهٔ پزشکی نیست.",
)

data class RetrievedContentChunk(
    val documentId: UUID,
    val versionId: UUID,
    val slug: String,
    val title: String,
    val excerpt: String,
    val locale: String,
)

data class AssistantGatewayRequest(
    val locale: String,
    val question: String,
    val redactedContext: String?,
    val retrievedChunks: List<RetrievedContentChunk>,
    val promptTemplateId: String,
)

data class AssistantGatewayResponse(
    val answer: String,
    val citations: List<ContentCitation>,
    val modelVersion: String,
    val providerId: String,
)

data class AssistantInteractionMetadata(
    val userId: UUID,
    val promptTemplateId: String,
    val providerId: String,
    val modelVersion: String,
    val outcomeClass: AssistantOutcomeClass,
)
