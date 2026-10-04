package dev.mahin.backend.assistant

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(
    properties = [
        "mahin.features.health-assistant=true",
        "mahin.assistant.provider=fake",
    ],
)
class AssistantEnabledIntegrationTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
) {
    @Test
    fun metaExposesHealthAssistantFlagWhenEnabled() {
        mockMvc.get("/v1/meta").andExpect {
            status { isOk() }
            jsonPath("$.featureFlags.health_assistant") { value(true) }
        }
    }

    @Test
    fun groundedAskReturnsCitation() {
        val token = registerUser()
        val json =
            mockMvc
                .post("/v1/assistant/ask") {
                    contentType = MediaType.APPLICATION_JSON
                    header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    content =
                        objectMapper.writeValueAsString(
                            AssistantAskRequest(
                                question = "نمونه fixture آموزشی",
                            ),
                        )
                }.andExpect {
                    status { isOk() }
                }.andReturn()
                .response
                .contentAsString
        val tree = objectMapper.readTree(json)
        assertEquals("ANSWERED", tree.get("outcome").asText())
        assertTrue(tree.get("citations").size() > 0)
    }

    @Test
    fun consentDefaultsOptOut() {
        val token = registerUser()
        val json =
            mockMvc
                .get("/v1/assistant/consent") {
                    header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                }.andExpect {
                    status { isOk() }
                }.andReturn()
                .response
                .contentAsString
        val scopes = objectMapper.readTree(json).get("scopes")
        assertFalse(scopes.get("shareCycleSummary").asBoolean())
        assertFalse(scopes.get("shareSymptomTags").asBoolean())
    }

    @Test
    fun trackerContextNotSentWithoutConsent() {
        val token = registerUser()
        mockMvc
            .put("/v1/assistant/consent") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                content =
                    objectMapper.writeValueAsString(
                        UpdateAssistantConsentRequest(
                            scopes = AssistantConsentScopes(shareCycleSummary = false),
                        ),
                    )
            }.andExpect { status { isOk() } }
        mockMvc
            .post("/v1/assistant/ask") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                content =
                    objectMapper.writeValueAsString(
                        AssistantAskRequest(
                            question = "نمونه fixture",
                            trackerContext =
                                AssistantTrackerContext(
                                    cycleSummary = "SECRET_CYCLE_SUMMARY",
                                ),
                        ),
                    )
            }.andExpect { status { isOk() } }
    }

    private fun registerUser(): String {
        val email = "assistant-on-${java.util.UUID.randomUUID()}@example.test"
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
