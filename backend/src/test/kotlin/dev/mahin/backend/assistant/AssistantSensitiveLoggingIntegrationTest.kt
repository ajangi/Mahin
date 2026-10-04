package dev.mahin.backend.assistant

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
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
class AssistantSensitiveLoggingIntegrationTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val capturingGateway: CapturingHealthAssistantGateway,
) {
    private lateinit var appender: ListAppender<ILoggingEvent>

    @BeforeEach
    fun attachLogAppender() {
        val root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
        appender = ListAppender()
        appender.start()
        root.addAppender(appender)
        capturingGateway.reset()
    }

    @AfterEach
    fun detachLogAppender() {
        val root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
        root.detachAppender(appender)
    }

    @Test
    fun askPathDoesNotLogQuestionContextOrAnswer() {
        val sensitiveQuestion = "یادداشت خصوصی سوال حساس m12-fixture-token"
        val sensitiveSummary = "SECRET_CYCLE_SUMMARY_VALUE"
        val sensitiveTag = "SECRET_SYMPTOM_TAG"
        val token = registerUser()
        mockMvc
            .post("/v1/assistant/ask") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                content =
                    objectMapper.writeValueAsString(
                        AssistantAskRequest(
                            question = sensitiveQuestion,
                            trackerContext =
                                AssistantTrackerContext(
                                    cycleSummary = sensitiveSummary,
                                    symptomTags = listOf(sensitiveTag),
                                ),
                        ),
                    )
            }.andExpect {
                status { isOk() }
            }
        val logs = appender.list.joinToString("\n") { it.formattedMessage }
        assertNoSensitiveLeak(logs, sensitiveQuestion, sensitiveSummary, sensitiveTag)
    }

    @Test
    fun escalationPathDoesNotLogSensitiveQuestion() {
        val sensitiveQuestion = "می‌خواهم بمیرم و یادداشت خصوصی"
        val token = registerUser()
        mockMvc
            .post("/v1/assistant/ask") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                content =
                    objectMapper.writeValueAsString(
                        AssistantAskRequest(question = sensitiveQuestion),
                    )
            }.andExpect {
                status { isOk() }
            }
        val logs = appender.list.joinToString("\n") { it.formattedMessage }
        assertNoSensitiveLeak(logs, sensitiveQuestion)
    }

    private fun assertNoSensitiveLeak(
        logs: String,
        vararg secrets: String,
    ) {
        secrets.forEach { secret ->
            org.junit.jupiter.api.Assertions
                .assertFalse(logs.contains(secret), "logs leaked: $secret")
        }
    }

    private fun registerUser(): String {
        val email = "assistant-log-${java.util.UUID.randomUUID()}@example.test"
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
