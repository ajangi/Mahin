package dev.mahin.backend.identity

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
class GuestConversionIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @Test
    fun attackerCannotConvertWithoutGuestProof() {
        val victimLocalUserId = UUID.randomUUID()
        bootstrapGuest(victimLocalUserId)

        val attackerToken = registerUser("attacker-${UUID.randomUUID()}@example.test")

        mockMvc
            .post("/v1/identity/convert-guest") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $attackerToken")
                content =
                    objectMapper.writeValueAsString(
                        mapOf("localUserId" to victimLocalUserId.toString()),
                    )
            }.andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun convertGuestWithProofMigratesData() {
        val localUserId = UUID.randomUUID()
        val guest = bootstrapGuest(localUserId)
        val entityId = UUID.randomUUID()
        pushMutation(
            guest.accessToken,
            entityId,
            Instant.parse("2026-07-01T00:00:00Z"),
            """{"marker":"guest"}""",
        )

        val userToken = registerUser("owner-${UUID.randomUUID()}@example.test")

        mockMvc
            .post("/v1/identity/convert-guest") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $userToken")
                content =
                    objectMapper.writeValueAsString(
                        mapOf(
                            "localUserId" to localUserId.toString(),
                            "guestAccessToken" to guest.accessToken,
                        ),
                    )
            }.andExpect {
                status { isOk() }
            }

        val pull =
            mockMvc
                .get("/v1/sync/changes?afterRevision=0") {
                    header(HttpHeaders.AUTHORIZATION, "Bearer $userToken")
                }.andExpect {
                    status { isOk() }
                }.andReturn()
                .response.contentAsString

        val marker =
            objectMapper
                .readTree(pull)
                .get("changes")
                .get(0)
                .get("payload")
                .get("marker")
                .asText()
        assert(marker == "guest")
    }

    @Test
    fun conversionCollisionKeepsNewerUserRow() {
        val localUserId = UUID.randomUUID()
        val guest = bootstrapGuest(localUserId)
        val entityId = UUID.randomUUID()

        val userToken = registerUser("collision-${UUID.randomUUID()}@example.test")
        pushMutation(
            userToken,
            entityId,
            Instant.parse("2026-08-02T00:00:00Z"),
            """{"winner":"user"}""",
        )
        pushMutation(
            guest.accessToken,
            entityId,
            Instant.parse("2026-08-01T00:00:00Z"),
            """{"winner":"guest"}""",
        )

        mockMvc
            .post("/v1/identity/convert-guest") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $userToken")
                content =
                    objectMapper.writeValueAsString(
                        mapOf(
                            "localUserId" to localUserId.toString(),
                            "guestRefreshToken" to guest.refreshToken,
                        ),
                    )
            }.andExpect {
                status { isOk() }
            }

        val pull =
            mockMvc
                .get("/v1/sync/changes?afterRevision=0") {
                    header(HttpHeaders.AUTHORIZATION, "Bearer $userToken")
                }.andReturn()
                .response.contentAsString
        val changes = objectMapper.readTree(pull).get("changes")
        var winner: String? = null
        for (index in 0 until changes.size()) {
            val change = changes.get(index)
            if (change.get("entityId").asText() == entityId.toString()) {
                winner = change.get("payload").get("winner").asText()
            }
        }
        assert(winner == "user")
    }

    private fun registerUser(email: String): String {
        val response =
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
        return objectMapper.readTree(response).get("accessToken").asText()
    }

    private fun bootstrapGuest(localUserId: UUID): GuestSession {
        val response =
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
        val tree = objectMapper.readTree(response)
        return GuestSession(
            accessToken = tree.get("accessToken").asText(),
            refreshToken = tree.get("refreshToken").asText(),
        )
    }

    private fun pushMutation(
        accessToken: String,
        entityId: UUID,
        updatedAt: Instant,
        payloadJson: String,
    ) {
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
                            "idempotencyKey" to "idem-${UUID.randomUUID()}",
                        ),
                    ),
            )
        mockMvc
            .post("/v1/sync/mutations") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                content = objectMapper.writeValueAsString(body)
            }.andExpect {
                status { isOk() }
            }
    }

    private data class GuestSession(
        val accessToken: String,
        val refreshToken: String,
    )
}
