package dev.mahin.backend.assistant

/**
 * Provider boundary for LLM-backed answers. No vendor SDK types leak past implementations.
 */
interface HealthAssistantGateway {
    fun ask(request: AssistantGatewayRequest): AssistantGatewayResponse

    fun isReady(): Boolean
}
