package dev.mahin.backend.assistant

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.assistant.persistence.AssistantConsentRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@SpringBootTest
@AutoConfigureMockMvc
class AssistantKillSwitchIntegrationTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val consentRepository: AssistantConsentRepository,
) {
    @Test
    fun askFailsClosedWhenRemoteKillSwitchOff() {
        val token = registerUser()
        mockMvc
            .post("/v1/assistant/ask") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                content =
                    objectMapper.writeValueAsString(
                        AssistantAskRequest(
                            question = "m12-fixture-token",
                        ),
                    )
            }.andExpect {
                status { isEqualTo(503) }
            }
    }

    @Test
    fun getConsentFailsClosedWhenKillSwitchOff() {
        val token = registerUser()
        mockMvc
            .get("/v1/assistant/consent") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            }.andExpect {
                status { isEqualTo(503) }
            }
    }

    @Test
    fun putConsentFailsClosedAndDoesNotPersist() {
        val token = registerUser()
        val before = consentRepository.count()
        mockMvc
            .put("/v1/assistant/consent") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                content =
                    objectMapper.writeValueAsString(
                        UpdateAssistantConsentRequest(
                            scopes = AssistantConsentScopes(shareCycleSummary = true),
                        ),
                    )
            }.andExpect {
                status { isEqualTo(503) }
            }
        org.junit.jupiter.api.Assertions
            .assertEquals(before, consentRepository.count())
    }

    private fun registerUser(): String {
        val email = "assistant-kill-${java.util.UUID.randomUUID()}@example.test"
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
