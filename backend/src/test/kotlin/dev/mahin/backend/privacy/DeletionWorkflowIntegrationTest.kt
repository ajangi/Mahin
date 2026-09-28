package dev.mahin.backend.privacy

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.auth.persistence.UserAccountRepository
import dev.mahin.backend.privacy.persistence.DeletionRequestRepository
import dev.mahin.backend.sync.persistence.SyncEntityRecordRepository
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
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["mahin.privacy.deletion-grace-seconds=0"])
class DeletionWorkflowIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val accountDeletionProcessor: AccountDeletionProcessor,
    @Autowired private val userAccountRepository: UserAccountRepository,
    @Autowired private val syncEntityRecordRepository: SyncEntityRecordRepository,
    @Autowired private val deletionRequestRepository: DeletionRequestRepository,
) {
    @Test
    fun accountDeletionPurgesSyncDataAndUserRow() {
        val email = "delete-me-${UUID.randomUUID()}@example.test"
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
        val entityId = UUID.randomUUID()
        pushMutation(token, entityId, Instant.parse("2026-09-01T00:00:00Z"), """{"marker":"sync"}""")

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
        assertTrue(syncEntityRecordRepository.findUserChangesAfter(userId, 0).isEmpty())
    }

    private fun pushMutation(
        accessToken: String,
        entityId: UUID,
        updatedAt: Instant,
        payloadJson: String,
    ) {
        mockMvc
            .post("/v1/sync/mutations") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                content =
                    objectMapper.writeValueAsString(
                        mapOf(
                            "mutations" to
                                listOf(
                                    mapOf(
                                        "entityType" to "period_record",
                                        "entityId" to entityId.toString(),
                                        "operation" to "UPSERT",
                                        "clientRevision" to 1,
                                        "updatedAt" to updatedAt.toString(),
                                        "payload" to objectMapper.readTree(payloadJson),
                                        "idempotencyKey" to "idem-${UUID.randomUUID()}",
                                    ),
                                ),
                        ),
                    )
            }.andExpect {
                status { isOk() }
            }
    }
}
