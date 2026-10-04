package dev.mahin.backend.assistant

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.assistant.persistence.AssistantConsentRepository
import dev.mahin.backend.assistant.persistence.AssistantInteractionLogRepository
import dev.mahin.backend.auth.persistence.UserAccountRepository
import dev.mahin.backend.privacy.AccountDeletionProcessor
import dev.mahin.backend.privacy.persistence.DeletionRequestRepository
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Suppress("LongParameterList")
@TestPropertySource(
    properties = [
        "mahin.privacy.deletion-grace-seconds=0",
        "mahin.features.health-assistant=true",
        "mahin.assistant.provider=fake",
    ],
)
class AssistantErasureIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val accountDeletionProcessor: AccountDeletionProcessor,
    @Autowired private val userAccountRepository: UserAccountRepository,
    @Autowired private val deletionRequestRepository: DeletionRequestRepository,
    @Autowired private val consentRepository: AssistantConsentRepository,
    @Autowired private val interactionLogRepository: AssistantInteractionLogRepository,
) {
    @Test
    fun accountDeletionRemovesAssistantConsentAndInteractionLogs() {
        val email = "assistant-erase-${UUID.randomUUID()}@example.test"
        val registerJson =
            mockMvc
                .post("/v1/auth/register") {
                    contentType = MediaType.APPLICATION_JSON
                    content =
                        objectMapper.writeValueAsString(
                            mapOf(
                                "email" to email,
                                "password" to "secure-password-12",
                                "platform" to "android",
                            ),
                        )
                }.andExpect {
                    status { isOk() }
                }.andReturn()
                .response
                .contentAsString
        val userId = UUID.fromString(objectMapper.readTree(registerJson).get("userId").asText())
        val token = objectMapper.readTree(registerJson).get("accessToken").asText()

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
                status { isOk() }
            }

        mockMvc
            .post("/v1/assistant/ask") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                content =
                    objectMapper.writeValueAsString(
                        AssistantAskRequest(question = "m12-fixture-token"),
                    )
            }.andExpect {
                status { isOk() }
            }

        assertTrue(consentRepository.findById(userId).isPresent)
        assertTrue(interactionLogRepository.countByUserId(userId) > 0)

        mockMvc
            .post("/v1/privacy/deletion-requests") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            }.andExpect {
                status { isOk() }
            }

        val pending =
            deletionRequestRepository.findAllByStatusAndScheduledAtLessThanEqual(
                AccountDeletionProcessor.STATUS_PENDING,
                Instant.now().plusSeconds(5),
            )
        assertEquals(1, pending.size)
        assertEquals(1, accountDeletionProcessor.processDueDeletions())

        assertTrue(userAccountRepository.findById(userId).isEmpty)
        assertTrue(consentRepository.findById(userId).isEmpty)
        assertEquals(0, interactionLogRepository.countByUserId(userId))
    }
}
