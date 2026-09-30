package dev.mahin.backend.privacy

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.privacy.persistence.DeletionRequestRepository
import dev.mahin.backend.security.SecurityAuditEventRepository
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Import(TransactionalErasureTestConfiguration::class)
@TestPropertySource(
    properties = [
        "mahin.privacy.deletion-grace-seconds=0",
        "mahin.privacy.deletion-max-attempts=2",
        "mahin.privacy.deletion-retry-base-seconds=60",
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
    private lateinit var accountDeletionExecutor: AccountDeletionExecutor

    @Autowired
    private lateinit var deletionRequestRepository: DeletionRequestRepository

    @Autowired
    private lateinit var securityAuditEventRepository: SecurityAuditEventRepository

    @BeforeEach
    fun resetFailureHooks() {
        TransactionalErasureFailureSupport.failUserIds.clear()
    }

    @Test
    fun transactionalErasureFailureSchedulesRetryThenPermanentFailure() {
        val email = "retry-delete-${UUID.randomUUID()}@example.test"
        val registerJson = registerUser(email)
        val userId = UUID.fromString(objectMapper.readTree(registerJson).get("userId").asText())
        val token = objectMapper.readTree(registerJson).get("accessToken").asText()
        val requestId = requestDeletion(token)
        TransactionalErasureFailureSupport.failUserIds.add(userId)

        val before = Instant.now()
        assertEquals(0, accountDeletionProcessor.processDueDeletions(before))

        val afterFirst = deletionRequestRepository.findById(requestId).orElseThrow()
        assertEquals(AccountDeletionProcessor.STATUS_PENDING, afterFirst.status)
        assertEquals(1, afterFirst.attemptCount)
        assertTrue(afterFirst.scheduledAt!!.isAfter(before))
        assertTrue(
            securityAuditEventRepository.findAll().any {
                it.action == "account_deletion_failed" && it.targetId == requestId.toString()
            },
        )

        assertEquals(
            0,
            accountDeletionProcessor.processDueDeletions(afterFirst.scheduledAt!!.minusSeconds(1)),
        )

        assertFalse(accountDeletionExecutor.processSingle(requestId, afterFirst.scheduledAt!!))
        val afterSecond = deletionRequestRepository.findById(requestId).orElseThrow()
        assertEquals(AccountDeletionProcessor.STATUS_FAILED, afterSecond.status)
        assertEquals(2, afterSecond.attemptCount)
        assertTrue(
            securityAuditEventRepository.findAll().any {
                it.action == "account_deletion_failed_permanent" &&
                    it.targetId == requestId.toString()
            },
        )
    }

    @Test
    fun oneFailedDeletionDoesNotBlockAnotherDueRequest() {
        val failingUser = registerUser("fail-delete-${UUID.randomUUID()}@example.test")
        val succeedingUser = registerUser("ok-delete-${UUID.randomUUID()}@example.test")
        val failingUserId = UUID.fromString(objectMapper.readTree(failingUser).get("userId").asText())
        val failingToken = objectMapper.readTree(failingUser).get("accessToken").asText()
        val succeedingToken = objectMapper.readTree(succeedingUser).get("accessToken").asText()
        val failingRequestId = requestDeletion(failingToken)
        requestDeletion(succeedingToken)
        TransactionalErasureFailureSupport.failUserIds.add(failingUserId)

        val now = Instant.now()
        assertEquals(1, accountDeletionProcessor.processDueDeletions(now))

        val failedRequest = deletionRequestRepository.findById(failingRequestId).orElseThrow()
        assertEquals(AccountDeletionProcessor.STATUS_PENDING, failedRequest.status)
        assertEquals(1, failedRequest.attemptCount)
        assertTrue(
            securityAuditEventRepository.findAll().none {
                it.action == "account_deletion_completed" && it.targetId == failingRequestId.toString()
            },
        )
    }

    private fun registerUser(email: String): String =
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

    private fun requestDeletion(accessToken: String): UUID {
        val body =
            mockMvc
                .post("/v1/privacy/deletion-requests") {
                    header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                }.andExpect {
                    status { isOk() }
                }.andReturn()
                .response.contentAsString
        return UUID.fromString(objectMapper.readTree(body).get("id").asText())
    }
}
