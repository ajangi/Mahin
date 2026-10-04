package dev.mahin.backend.assistant

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

@Configuration
@EnableConfigurationProperties(AssistantProperties::class)
class AssistantConfiguration {
    @Bean
    fun healthAssistantGateway(
        properties: AssistantProperties,
        environment: Environment,
    ): HealthAssistantGateway {
        val allowFakeProvider =
            environment.acceptsProfiles(Profiles.of("local", "test", "dev"))
        return when (properties.provider) {
            AssistantProperties.PROVIDER_FAKE ->
                if (allowFakeProvider) {
                    DeterministicFakeHealthAssistantGateway()
                } else {
                    UnconfiguredHealthAssistantGateway()
                }
            AssistantProperties.PROVIDER_OPENAI -> OpenAiHealthAssistantGateway(properties)
            else -> UnconfiguredHealthAssistantGateway()
        }
    }
}
