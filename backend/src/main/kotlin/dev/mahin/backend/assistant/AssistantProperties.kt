package dev.mahin.backend.assistant

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mahin.assistant")
data class AssistantProperties(
    val provider: String = PROVIDER_NONE,
    val promptTemplateId: String = "mahin-assistant-v0-placeholder",
    val openaiApiKey: String = "",
    val openaiModel: String = "gpt-4o-mini",
) {
    fun isProviderConfigured(): Boolean = provider == PROVIDER_FAKE || provider == PROVIDER_OPENAI

    companion object {
        const val PROVIDER_NONE = "none"
        const val PROVIDER_FAKE = "fake"
        const val PROVIDER_OPENAI = "openai"
    }
}
