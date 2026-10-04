package dev.mahin.backend.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class MetaAndHealthTest(
    @Autowired val mockMvc: MockMvc,
) {
    @Test
    fun healthIsUp() {
        mockMvc.get("/actuator/health").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("UP") }
        }
    }

    @Test
    fun metaIsNonSensitive() {
        mockMvc.get("/v1/meta").andExpect {
            status { isOk() }
            jsonPath("$.product") { value("mahin") }
            jsonPath("$.apiVersion") { value("0.0.1") }
            jsonPath("$.featureFlags.health_connect") { value(false) }
            jsonPath("$.featureFlags.health_assistant") { value(false) }
        }
    }

    @Test
    fun openApiDocsArePublished() {
        mockMvc.get("/v3/api-docs").andExpect {
            status { isOk() }
        }
    }
}
