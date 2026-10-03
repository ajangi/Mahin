package dev.mahin.backend.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["mahin.features.health-connect=true"])
class MetaFeatureFlagsIntegrationTest(
    @Autowired val mockMvc: MockMvc,
) {
    @Test
    fun metaExposesHealthConnectLaunchFlagWhenEnabled() {
        mockMvc.get("/v1/meta").andExpect {
            status { isOk() }
            jsonPath("$.featureFlags.health_connect") { value(true) }
        }
    }
}
