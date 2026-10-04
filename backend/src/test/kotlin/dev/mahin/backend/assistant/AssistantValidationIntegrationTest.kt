package dev.mahin.backend.assistant

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(
    properties = [
        "mahin.features.health-assistant=true",
        "mahin.assistant.provider=fake",
    ],
)
class AssistantValidationIntegrationTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
) {
    @Test
    fun oversizedQuestionReturnsValidationError() {
        val token = registerUser()
        val longQuestion = "a".repeat(2001)
        postAsk(token, AssistantAskRequest(question = longQuestion))
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("validation_error") }
            }
    }

    @Test
    fun blankQuestionReturnsValidationError() {
        val token = registerUser()
        postAsk(token, AssistantAskRequest(question = "   "))
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("validation_error") }
            }
    }

    @Test
    fun oversizedCycleSummaryReturnsValidationError() {
        val token = registerUser()
        postAsk(
            token,
            AssistantAskRequest(
                question = "m12-fixture-token",
                trackerContext =
                    AssistantTrackerContext(
                        cycleSummary = "x".repeat(513),
                    ),
            ),
        ).andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("validation_error") }
        }
    }

    @Test
    fun oversizedSymptomTagReturnsValidationError() {
        val token = registerUser()
        postAsk(
            token,
            AssistantAskRequest(
                question = "m12-fixture-token",
                trackerContext =
                    AssistantTrackerContext(
                        symptomTags = listOf("t".repeat(65)),
                    ),
            ),
        ).andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("validation_error") }
        }
    }

    @Test
    fun tooManySymptomTagsReturnsValidationError() {
        val token = registerUser()
        postAsk(
            token,
            AssistantAskRequest(
                question = "m12-fixture-token",
                trackerContext =
                    AssistantTrackerContext(
                        symptomTags = List(21) { "tag-$it" },
                    ),
            ),
        ).andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("validation_error") }
        }
    }

    private fun postAsk(
        token: String,
        request: AssistantAskRequest,
    ) = mockMvc.post("/v1/assistant/ask") {
        contentType = MediaType.APPLICATION_JSON
        header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        content = objectMapper.writeValueAsString(request)
    }

    private fun registerUser(): String {
        val email = "assistant-val-${java.util.UUID.randomUUID()}@example.test"
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
