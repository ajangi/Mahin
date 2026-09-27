package dev.mahin.backend.notifications

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.device.persistence.DeviceInstallationRepository
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class PushTokenIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val deviceInstallationRepository: DeviceInstallationRepository,
) {
    @Test
    fun guestCanRegisterAndClearPushTokenHashWithoutStoringRawToken() {
        val rawToken = "sample-fcm-token-should-not-be-logged"
        val localUserId = UUID.randomUUID()
        val bootstrap =
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
        val tree = objectMapper.readTree(bootstrap)
        val accessToken = tree.get("accessToken").asText()
        val deviceId = UUID.fromString(tree.get("deviceId").asText())

        mockMvc
            .put("/v1/devices/$deviceId/push-token") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                content =
                    objectMapper.writeValueAsString(
                        mapOf(
                            "provider" to "fcm",
                            "token" to rawToken,
                        ),
                    )
            }.andExpect {
                status { isOk() }
                jsonPath("$.deviceId").value(deviceId.toString())
                jsonPath("$.provider").value("fcm")
            }

        val device = deviceInstallationRepository.findById(deviceId).orElseThrow()
        assertEquals(sha256Hex(rawToken), device.pushTokenHash)
        assertNotEquals(rawToken, device.pushTokenHash)

        mockMvc
            .put("/v1/devices/$deviceId/push-token") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    objectMapper.writeValueAsString(
                        mapOf(
                            "provider" to "fcm",
                            "token" to rawToken,
                        ),
                    )
            }.andExpect {
                // Stateless JWT filter: missing credentials → 403 (not authenticated)
                status { isForbidden() }
            }

        mockMvc
            .delete("/v1/devices/$deviceId/push-token") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
            }
    }

    private fun sha256Hex(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(value.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
