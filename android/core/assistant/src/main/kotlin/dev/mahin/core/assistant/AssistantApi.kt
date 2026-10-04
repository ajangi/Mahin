package dev.mahin.core.assistant

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

@Serializable
data class AssistantConsentScopesDto(
    val shareCycleSummary: Boolean = false,
    val shareSymptomTags: Boolean = false,
)

@Serializable
data class AssistantConsentResponseDto(
    val scopes: AssistantConsentScopesDto,
    val updatedAtEpochMs: Long? = null,
)

@Serializable
data class UpdateAssistantConsentRequestDto(
    val scopes: AssistantConsentScopesDto,
)

@Serializable
data class AssistantTrackerContextDto(
    val cycleSummary: String? = null,
    val symptomTags: List<String> = emptyList(),
)

@Serializable
data class AssistantAskRequestDto(
    val locale: String = "fa-IR",
    val question: String,
    val trackerContext: AssistantTrackerContextDto? = null,
)

@Serializable
data class ContentCitationDto(
    val documentId: String,
    val versionId: String,
    val title: String,
    val slug: String,
)

@Serializable
data class AssistantAskResponseDto(
    val outcome: String,
    val answer: String? = null,
    val citations: List<ContentCitationDto> = emptyList(),
    val escalationCode: String? = null,
    val refusalCode: String? = null,
    val disclaimer: String? = null,
)

interface AssistantApi {
    @GET("v1/assistant/consent")
    suspend fun getConsent(
        @Header("Authorization") authorization: String,
    ): AssistantConsentResponseDto

    @PUT("v1/assistant/consent")
    suspend fun updateConsent(
        @Header("Authorization") authorization: String,
        @Body body: UpdateAssistantConsentRequestDto,
    ): AssistantConsentResponseDto

    @POST("v1/assistant/ask")
    suspend fun ask(
        @Header("Authorization") authorization: String,
        @Body body: AssistantAskRequestDto,
    ): AssistantAskResponseDto
}
