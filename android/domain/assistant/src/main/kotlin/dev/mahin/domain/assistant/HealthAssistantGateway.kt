package dev.mahin.domain.assistant

/**
 * Domain boundary for future assistant providers (PRD §24). No vendor SDK types here.
 */
interface HealthAssistantGateway {
    suspend fun ask(input: AssistantAskInput): Result<AssistantAnswer>

    suspend fun refreshConsent(): Result<AssistantConsentState>

    suspend fun updateConsent(scopes: AssistantConsentScopes): Result<AssistantConsentState>
}
