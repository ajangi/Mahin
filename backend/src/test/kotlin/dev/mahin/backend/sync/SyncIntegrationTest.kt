package dev.mahin.backend.sync

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class SyncIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @Test
    fun guestSyncIsIdempotentAndPullableAcrossSessions() {
        val localUserId = UUID.randomUUID()
        val guestTokens = bootstrapGuest(localUserId)
        val entityId = UUID.randomUUID()
        val updatedAt = Instant.parse("2026-03-01T12:00:00Z")
        val idempotencyKey = "idem-${UUID.randomUUID()}"

        val first =
            pushMutation(
                guestTokens.accessToken,
                entityId,
                updatedAt,
                idempotencyKey,
                """{"cycleDay":3}""",
            )
        val second =
            pushMutation(
                guestTokens.accessToken,
                entityId,
                updatedAt,
                idempotencyKey,
                """{"cycleDay":3}""",
            )

        assertJsonPath(first, "$.results[0].status").isEqualTo("applied")
        assertJsonPath(second, "$.results[0].status").isEqualTo("applied")
        assertJsonPath(first, "$.results[0].serverRevision").isEqualTo(
            objectMapper
                .readTree(second)
                .get("results")
                .get(0)
                .get("serverRevision")
                .asText(),
        )

        val guestTokensDeviceTwo = bootstrapGuest(localUserId)
        val pull =
            mockMvc
                .get("/v1/sync/changes?afterRevision=0") {
                    header(HttpHeaders.AUTHORIZATION, "Bearer ${guestTokensDeviceTwo.accessToken}")
                }.andExpect {
                    status { isOk() }
                }.andReturn()
                .response.contentAsString

        val pullTree = objectMapper.readTree(pull)
        assert(pullTree.get("changes").size() == 1)
        assert(
            pullTree
                .get("changes")
                .get(0)
                .get("entityId")
                .asText() == entityId.toString(),
        )
    }

    @Test
    fun guestConversionPreservesEntityIdsForRegisteredAccount() {
        val localUserId = UUID.randomUUID()
        val guest = bootstrapGuest(localUserId)
        val entityId = UUID.randomUUID()
        pushMutation(
            guest.accessToken,
            entityId,
            Instant.parse("2026-04-01T00:00:00Z"),
            "convert-idem-${UUID.randomUUID()}",
            """{"note":"placeholder-non-medical"}""",
        )

        val email = "user-${UUID.randomUUID()}@example.test"
        val registerBody =
            mapOf(
                "email" to email,
                "password" to "secure-password-12",
                "platform" to "android",
                "localUserId" to localUserId.toString(),
                "guestRefreshToken" to guest.refreshToken,
            )
        val registerResponse =
            mockMvc
                .post("/v1/auth/register") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(registerBody)
                }.andExpect {
                    status { isOk() }
                }.andReturn()
                .response.contentAsString

        val userToken = objectMapper.readTree(registerResponse).get("accessToken").asText()

        val pull =
            mockMvc
                .get("/v1/sync/changes?afterRevision=0") {
                    header(HttpHeaders.AUTHORIZATION, "Bearer $userToken")
                }.andExpect {
                    status { isOk() }
                }.andReturn()
                .response.contentAsString

        val changes = objectMapper.readTree(pull).get("changes")
        assert(changes.size() == 1)
        assert(changes.get(0).get("entityId").asText() == entityId.toString())
    }

    @Test
    fun staleUpsertConflictsWithoutDroppingNewerServerRow() {
        val localUserId = UUID.randomUUID()
        val guest = bootstrapGuest(localUserId)
        val entityId = UUID.randomUUID()
        pushMutation(
            guest.accessToken,
            entityId,
            Instant.parse("2026-05-02T00:00:00Z"),
            "newer-${UUID.randomUUID()}",
            """{"v":2}""",
        )
        val conflict =
            pushMutation(
                guest.accessToken,
                entityId,
                Instant.parse("2026-05-01T00:00:00Z"),
                "stale-${UUID.randomUUID()}",
                """{"v":1}""",
            )
        assertJsonPath(conflict, "$.results[0].status").isEqualTo("conflict")

        val pull =
            mockMvc
                .get("/v1/sync/changes?afterRevision=0") {
                    header(HttpHeaders.AUTHORIZATION, "Bearer ${guest.accessToken}")
                }.andReturn()
                .response.contentAsString
        val payload =
            objectMapper
                .readTree(pull)
                .get("changes")
                .get(0)
                .get("payload")
                .get("v")
                .asInt()
        assert(payload == 2)
    }

    @Test
    fun staleDeleteConflictsWithoutRemovingNewerUpsert() {
        val localUserId = UUID.randomUUID()
        val guest = bootstrapGuest(localUserId)
        val entityId = UUID.randomUUID()
        pushMutation(
            guest.accessToken,
            entityId,
            Instant.parse("2026-05-10T00:00:00Z"),
            "upsert-${UUID.randomUUID()}",
            """{"v":2}""",
        )
        val conflict =
            pushDelete(
                guest.accessToken,
                entityId,
                Instant.parse("2026-05-09T00:00:00Z"),
                "delete-${UUID.randomUUID()}",
            )
        assertJsonPath(conflict, "$.results[0].status").isEqualTo("conflict")
        assertJsonPath(conflict, "$.results[0].conflictCode").isEqualTo("updated_at_stale")

        val pull =
            mockMvc
                .get("/v1/sync/changes?afterRevision=0") {
                    header(HttpHeaders.AUTHORIZATION, "Bearer ${guest.accessToken}")
                }.andReturn()
                .response.contentAsString
        assert(
            objectMapper
                .readTree(pull)
                .get("changes")
                .get(0)
                .get("payload")
                .get("v")
                .asInt() == 2,
        )
    }

    private fun bootstrapGuest(localUserId: UUID): GuestSession {
        val body =
            mapOf(
                "localUserId" to localUserId.toString(),
                "platform" to "android",
            )
        val response =
            mockMvc
                .post("/v1/identity/guest") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(body)
                }.andExpect {
                    status { isOk() }
                }.andReturn()
                .response.contentAsString
        val tree = objectMapper.readTree(response)
        return GuestSession(
            accessToken = tree.get("accessToken").asText(),
            refreshToken = tree.get("refreshToken").asText(),
            guestInstallationId = UUID.fromString(tree.get("guestInstallationId").asText()),
        )
    }

    private fun pushDelete(
        accessToken: String,
        entityId: UUID,
        updatedAt: Instant,
        idempotencyKey: String,
    ): String {
        val body =
            mapOf(
                "mutations" to
                    listOf(
                        mapOf(
                            "entityType" to "period_record",
                            "entityId" to entityId.toString(),
                            "operation" to "DELETE",
                            "updatedAt" to updatedAt.toString(),
                            "idempotencyKey" to idempotencyKey,
                        ),
                    ),
            )
        return mockMvc
            .post("/v1/sync/mutations") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                content = objectMapper.writeValueAsString(body)
            }.andExpect {
                status { isOk() }
            }.andReturn()
            .response.contentAsString
    }

    private fun pushMutation(
        accessToken: String,
        entityId: UUID,
        updatedAt: Instant,
        idempotencyKey: String,
        payloadJson: String,
    ): String {
        val body =
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
                            "idempotencyKey" to idempotencyKey,
                        ),
                    ),
            )
        return mockMvc
            .post("/v1/sync/mutations") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                content = objectMapper.writeValueAsString(body)
            }.andExpect {
                status { isOk() }
            }.andReturn()
            .response.contentAsString
    }

    private fun assertJsonPath(
        json: String,
        path: String,
    ): org.assertj.core.api.AbstractStringAssert<*> {
        val tree = objectMapper.readTree(json)
        val pointer = path.removePrefix("$.").split(".")
        var node = tree
        pointer.forEach { segment ->
            node =
                if (segment.contains("[")) {
                    val field = segment.substringBefore("[")
                    val index = segment.substringAfter("[").removeSuffix("]").toInt()
                    node.get(field).get(index)
                } else {
                    node.get(segment)
                }
        }
        return org.assertj.core.api.Assertions
            .assertThat(node.asText())
    }

    private data class GuestSession(
        val accessToken: String,
        val refreshToken: String,
        val guestInstallationId: UUID,
    )
}
