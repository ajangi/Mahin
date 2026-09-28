package dev.mahin.backend.security

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header

@SpringBootTest
@AutoConfigureMockMvc
class SecurityHeadersIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @Test
    fun metaEndpointIncludesSecurityHeaders() {
        mockMvc
            .get("/v1/meta")
            .andExpect {
                header().string("X-Content-Type-Options", "nosniff")
                header().string("X-Frame-Options", "DENY")
                header().exists("Content-Security-Policy")
            }
    }
}
