package dev.mahin.backend.privacy

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.auth.persistence.UserAccountRepository
import dev.mahin.backend.device.persistence.DeviceInstallationRepository
import dev.mahin.backend.identity.persistence.GuestInstallationRepository
import dev.mahin.backend.privacy.persistence.DeletionRequestRepository
import dev.mahin.backend.security.SecurityAuditEventRepository
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
@Suppress("LongParameterList")
class DeletionWorkflowIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val accountDeletionProcessor: AccountDeletionProcessor,
    @Autowired private val userAccountRepository: UserAccountRepository,
    @Autowired private val syncEntityRecordRepository: SyncEntityRecordRepository,
    @Autowired private val deletionRequestRepository: DeletionRequestRepository,
    @Autowired private val securityAuditEventRepository: SecurityAuditEventRepository,
    @Autowired private val guestInstallationRepository: GuestInstallationRepository,
    @Autowired private val deviceInstallationRepository: DeviceInstallationRepository,
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
        assertTrue(deletionRequestRepository.findAll().isEmpty())
        assertTrue(
            securityAuditEventRepository.findAll().any { it.action == "account_deletion_completed" },
        )
    }

    @Test
    fun guestConvertedUserDeletionPurgesGuestArtifacts() {
        val localUserId = UUID.randomUUID()
        val guestJson =
            mockMvc
                .post("/v1/identity/guest") {
                    contentType = MediaType.APPLICATION_JSON
                    content =
                        objectMapper.writeValueAsString(
                            mapOf(
                                "localUserId" to localUserId.toString(),
                                "platform" to "android",
                            ),
                        )
                }.andExpect {
                    status { isOk() }
                }.andReturn()
                .response.contentAsString
        val guestTree = objectMapper.readTree(guestJson)
        val guestAccessToken = guestTree.get("accessToken").asText()
        val guestInstallationId = UUID.fromString(guestTree.get("guestInstallationId").asText())
        val entityId = UUID.randomUUID()
        pushMutation(guestAccessToken, entityId, Instant.parse("2026-09-01T00:00:00Z"), """{"marker":"guest"}""")

        val email = "guest-convert-delete-${UUID.randomUUID()}@example.test"
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
                                "localUserId" to localUserId.toString(),
                                "guestAccessToken" to guestAccessToken,
                            ),
                        )
                }.andExpect {
                    status { isOk() }
                }.andReturn()
                .response.contentAsString
        val userId = UUID.fromString(objectMapper.readTree(registerJson).get("userId").asText())
        val token = objectMapper.readTree(registerJson).get("accessToken").asText()

        assertTrue(guestInstallationRepository.findById(guestInstallationId).isPresent)
        assertTrue(deviceInstallationRepository.findAllByOwnerUserId(userId).isNotEmpty())

        mockMvc
            .post("/v1/privacy/deletion-requests") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            }.andExpect {
                status { isOk() }
            }

        assertEquals(1, accountDeletionProcessor.processDueDeletions())

        assertTrue(userAccountRepository.findById(userId).isEmpty)
        assertTrue(guestInstallationRepository.findById(guestInstallationId).isEmpty)
        assertTrue(deviceInstallationRepository.findAllByOwnerUserId(userId).isEmpty())
        assertTrue(deletionRequestRepository.findAll().isEmpty())
        assertTrue(
            securityAuditEventRepository.findAll().any { it.action == "account_deletion_completed" },
        )
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
