package dev.mahin.backend.media

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class MediaAssetContractTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val urlFactory: MediaPublicUrlFactory,
) {
    @Test
    fun placeholderAssetUsesConfiguredBaseAndNoQuery() {
        mockMvc.get("/v1/media/assets/11111111-1111-1111-1111-111111111111").andExpect {
            status { isOk() }
            jsonPath("$.storageKey") { value("placeholders/non-medical/foundation-mark/v1") }
            jsonPath("$.medicalGoverned") { value(false) }
            jsonPath("$.publicUrl") {
                value("http://localhost:9000/mahin-media/placeholders/non-medical/foundation-mark/v1")
            }
        }
    }

    @Test
    fun unknownAssetIsNotFound() {
        mockMvc.get("/v1/media/assets/33333333-3333-3333-3333-333333333333").andExpect {
            status { isNotFound() }
            jsonPath("$.code") { value("media_not_found") }
        }
    }

    @Test
    fun factoryRejectsUserEncodedKeys() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            urlFactory.urlFor("users/abc/cycle.png")
        }
    }
}
