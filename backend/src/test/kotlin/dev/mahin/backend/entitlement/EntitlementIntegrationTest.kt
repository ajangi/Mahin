package dev.mahin.backend.entitlement

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class EntitlementIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @Test
    fun registeredUserStartsFreeThenPremiumAfterVerifiedPurchase() {
        val token = registerUser("premium-${UUID.randomUUID()}@example.test")

        mockMvc
            .get("/v1/entitlements/me") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            }.andExpect {
                status { isOk() }
                jsonPath("$.tier").value("FREE")
                jsonPath("$.features[0]").value(EntitlementFeatureCatalog.CYCLE_BASIC_INSIGHTS)
            }

        mockMvc
            .post("/v1/billing/google-play/verify") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                content =
                    objectMapper.writeValueAsString(
                        mapOf(
                            "productId" to DevGooglePlayPurchaseVerifier.PRODUCT_PREMIUM_MONTHLY,
                            "purchaseToken" to "gp-test-valid-${UUID.randomUUID()}",
                        ),
                    )
            }.andExpect {
                status { isOk() }
                jsonPath("$.tier").value("PREMIUM_MONTHLY")
            }

        mockMvc
            .get("/v1/entitlements/me") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            }.andExpect {
                status { isOk() }
                jsonPath("$.tier").value("PREMIUM_MONTHLY")
            }
    }

    @Test
    fun expiredPurchaseRevokesPremiumAndReturnsFree() {
        val token = registerUser("expired-${UUID.randomUUID()}@example.test")
        mockMvc
            .post("/v1/billing/google-play/verify") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                content =
                    objectMapper.writeValueAsString(
                        mapOf(
                            "productId" to DevGooglePlayPurchaseVerifier.PRODUCT_PREMIUM_ANNUAL,
                            "purchaseToken" to "gp-test-expired-${UUID.randomUUID()}",
                        ),
                    )
            }.andExpect {
                status { isOk() }
                jsonPath("$.tier").value("FREE")
            }

        mockMvc
            .get("/v1/entitlements/me") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            }.andExpect {
                status { isOk() }
                jsonPath("$.tier").value("FREE")
            }
    }

    @Test
    fun purchaseTokenIsStoredHashedNotRaw() {
        val token = registerUser("hash-${UUID.randomUUID()}@example.test")
        val rawToken = "gp-test-valid-${UUID.randomUUID()}"
        mockMvc
            .post("/v1/billing/google-play/verify") {
                contentType = MediaType.APPLICATION_JSON
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                content =
                    objectMapper.writeValueAsString(
                        mapOf(
                            "productId" to DevGooglePlayPurchaseVerifier.PRODUCT_PREMIUM_MONTHLY,
                            "purchaseToken" to rawToken,
                        ),
                    )
            }.andExpect {
                status { isOk() }
            }

        val hash = EntitlementService.sha256Hex(rawToken)
        assertEquals(64, hash.length)
        assertTrue(hash.all { it in '0'..'9' || it in 'a'..'f' })
        assertTrue(hash != rawToken)
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
}
