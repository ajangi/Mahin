package dev.mahin.backend.assistant

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UnconfiguredHealthAssistantGateway : HealthAssistantGateway {
    override fun isReady(): Boolean = false

    override fun ask(request: AssistantGatewayRequest): AssistantGatewayResponse =
        throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "assistant_not_configured")
}

/**
 * Deterministic fake provider for local/test only. Never enabled in production profiles.
 */
class DeterministicFakeHealthAssistantGateway : HealthAssistantGateway {
    override fun isReady(): Boolean = true

    override fun ask(request: AssistantGatewayRequest): AssistantGatewayResponse {
        val primary = request.retrievedChunks.first()
        val answer =
            buildString {
                append("پاسخ آزمایشی (غیرپزشکی) بر اساس محتوای تأییدشده: ")
                append(primary.title)
                append(". ")
                append(primary.excerpt)
            }
        return AssistantGatewayResponse(
            answer = answer,
            citations =
                request.retrievedChunks.map {
                    ContentCitation(
                        documentId = it.documentId.toString(),
                        versionId = it.versionId.toString(),
                        title = it.title,
                        slug = it.slug,
                    )
                },
            modelVersion = "fake-deterministic-v0",
            providerId = AssistantProperties.PROVIDER_FAKE,
        )
    }
}

/**
 * Production vendor adapter stub — fails closed without configured API key.
 * Real HTTP calls are **prepared, not executed** until DPA/residency and keys are provisioned.
 */
class OpenAiHealthAssistantGateway(
    private val properties: AssistantProperties,
) : HealthAssistantGateway {
    override fun isReady(): Boolean = properties.openaiApiKey.isNotBlank()

    override fun ask(request: AssistantGatewayRequest): AssistantGatewayResponse {
        if (properties.openaiApiKey.isBlank()) {
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "assistant_provider_not_configured")
        }
        throw ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "openai_adapter_prepared_not_executed")
    }
}
