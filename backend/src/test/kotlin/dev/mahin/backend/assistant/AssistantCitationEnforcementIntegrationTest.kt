package dev.mahin.backend.assistant

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(CapturingAssistantGatewayTestConfiguration::class)
@TestPropertySource(
    properties = [
        "mahin.features.health-assistant=true",
        "mahin.assistant.provider=fake",
    ],
)
class AssistantCitationEnforcementIntegrationTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val capturingGateway: CapturingHealthAssistantGateway,
) {
    @Test
    fun emptyCitationsReturnErrorOutcome() {
        capturingGateway.reset()
        capturingGateway.responseOverride =
            AssistantGatewayResponse(
                answer = "should not surface",
                citations = emptyList(),
                modelVersion = "stub",
                providerId = "stub",
            )
        val token = registerUser()
        val json =
            mockMvc
                .post("/v1/assistant/ask") {
                    contentType = MediaType.APPLICATION_JSON
                    header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    content =
                        objectMapper.writeValueAsString(
                            AssistantAskRequest(question = "m12-fixture-token"),
                        )
                }.andExpect { status { isOk() } }
                .andReturn()
                .response
                .contentAsString
        assertEquals("ERROR", objectMapper.readTree(json).get("outcome").asText())
    }

    @Test
    fun foreignCitationIdReturnsErrorOutcome() {
        capturingGateway.reset()
        capturingGateway.responseOverride =
            AssistantGatewayResponse(
                answer = "should not surface",
                citations =
                    listOf(
                        ContentCitation(
                            documentId = "00000000-0000-0000-0000-000000000099",
                            versionId = "00000000-0000-0000-0000-000000000098",
                            title = "x",
                            slug = "x",
                        ),
                    ),
                modelVersion = "stub",
                providerId = "stub",
            )
        val token = registerUser()
        val json =
            mockMvc
                .post("/v1/assistant/ask") {
                    contentType = MediaType.APPLICATION_JSON
                    header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    content =
                        objectMapper.writeValueAsString(
                            AssistantAskRequest(question = "m12-fixture-token"),
                        )
                }.andExpect { status { isOk() } }
                .andReturn()
                .response
                .contentAsString
        assertEquals("ERROR", objectMapper.readTree(json).get("outcome").asText())
    }

    private fun registerUser(): String {
        val email = "assistant-cite-${java.util.UUID.randomUUID()}@example.test"
        val json =
            mockMvc
                .post("/v1/auth/register") {
                    contentType = MediaType.APPLICATION_JSON
                    content =
                        """
                        {
                          "email": "$email",
                          "password": "password-12345678",
                          "platform": "android",
                          "appVersion": "1.0.0"
                        }
                        """.trimIndent()
                }.andReturn()
                .response
                .contentAsString
        return objectMapper.readTree(json).get("accessToken").asText()
    }
}
