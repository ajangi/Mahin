package dev.mahin.backend.assistant

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

class CapturingHealthAssistantGateway : HealthAssistantGateway {
    override fun isReady(): Boolean = true

    val requests = mutableListOf<AssistantGatewayRequest>()
    var responseOverride: AssistantGatewayResponse? = null

    fun reset() {
        requests.clear()
        responseOverride = null
    }

    override fun ask(request: AssistantGatewayRequest): AssistantGatewayResponse {
        requests.add(request)
        return responseOverride ?: DeterministicFakeHealthAssistantGateway().ask(request)
    }
}

@TestConfiguration
class CapturingAssistantGatewayTestConfiguration {
    @Bean
    @Primary
    fun capturingHealthAssistantGateway(): CapturingHealthAssistantGateway = CapturingHealthAssistantGateway()
}
