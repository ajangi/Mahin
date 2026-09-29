package dev.mahin.backend.privacy

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.privacy.persistence.DeletionRequestRepository
import dev.mahin.backend.security.SecurityAuditEventRepository
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Import(AccountDeletionRetryIntegrationTest.FailingErasureConfig::class)
@TestPropertySource(
    properties = [
        "mahin.privacy.deletion-grace-seconds=0",
        "mahin.privacy.deletion-max-attempts=2",
        "mahin.privacy.deletion-retry-base-seconds=0",
    ],
)
class AccountDeletionRetryIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var accountDeletionProcessor: AccountDeletionProcessor

    @Autowired
    private lateinit var deletionRequestRepository: DeletionRequestRepository

    @Autowired
    private lateinit var securityAuditEventRepository: SecurityAuditEventRepository

    @Test
    fun erasureFailureRetriesThenMarksPermanentFailure() {
        val email = "retry-delete-${UUID.randomUUID()}@example.test"
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
                .response.contentAsString
        val userId = UUID.fromString(objectMapper.readTree(registerJson).get("userId").asText())
        val token = objectMapper.readTree(registerJson).get("accessToken").asText()

        mockMvc
            .post("/v1/privacy/deletion-requests") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            }.andExpect {
                status { isOk() }
            }

        assertEquals(0, accountDeletionProcessor.processDueDeletions())
        val afterFirst = deletionRequestRepository.findAll().single()
        assertEquals(AccountDeletionProcessor.STATUS_PENDING, afterFirst.status)
        assertEquals(1, afterFirst.attemptCount)
        assertEquals(userId, afterFirst.userId)
        assertTrue(
            securityAuditEventRepository.findAll().any { it.action == "account_deletion_failed" },
        )

        assertEquals(0, accountDeletionProcessor.processDueDeletions())
        val afterSecond = deletionRequestRepository.findAll().single()
        assertEquals(AccountDeletionProcessor.STATUS_FAILED, afterSecond.status)
        assertEquals(2, afterSecond.attemptCount)
        assertTrue(
            securityAuditEventRepository.findAll().any {
                it.action == "account_deletion_failed_permanent"
            },
        )
    }

    @TestConfiguration
    class FailingErasureConfig {
        @Bean
        @Primary
        fun registeredUserErasure(): RegisteredUserErasure =
            RegisteredUserErasure { _ ->
                throw IllegalStateException("boom")
            }
    }
}
