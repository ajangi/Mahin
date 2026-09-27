package dev.mahin.backend.content

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class ContentEnvelopeTest(
    @Autowired val mockMvc: MockMvc,
) {
    @Test
    fun draftEnvelopeIsNotPublic() {
        mockMvc.get("/v1/content/articles/22222222-2222-2222-2222-222222222222").andExpect {
            status { isNotFound() }
        }
    }
}
