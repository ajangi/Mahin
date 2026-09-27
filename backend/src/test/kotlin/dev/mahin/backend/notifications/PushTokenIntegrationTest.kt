package dev.mahin.backend.notifications

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
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
) {
    @Test
    fun guestCanRegisterAndClearPushTokenHashWithoutStoringRawToken() {
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
        val deviceId = tree.get("deviceId").asText()

        mockMvc
            .put("/v1/devices/$deviceId/push-token") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                content =
                    objectMapper.writeValueAsString(
                        mapOf(
                            "provider" to "fcm",
                            "token" to "sample-fcm-token-should-not-be-logged",
                        ),
                    )
            }.andExpect {
                status { isOk() }
                jsonPath("$.deviceId").value(deviceId)
                jsonPath("$.provider").value("fcm")
            }

        mockMvc
            .delete("/v1/devices/$deviceId/push-token") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }.andExpect {
                status { isOk() }
            }
    }
}
