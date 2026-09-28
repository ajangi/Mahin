package dev.mahin.backend.cms

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class CmsLeastPrivilegeIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val cmsAuthService: CmsAuthService,
) {
    @Test
    fun supportRoleCannotCreateDocuments() {
        val suffix = UUID.randomUUID().toString().take(8)
        cmsAuthService.createStaff(
            email = "support-$suffix@mahin.test",
            password = "password-12-chars",
            displayName = "Support",
            roles = setOf(CmsRole.SUPPORT),
        )
        val token =
            cmsAuthService
                .login(
                    dev.mahin.backend.content.AdminLoginRequest(
                        email = "support-$suffix@mahin.test",
                        password = "password-12-chars",
                    ),
                ).accessToken

        mockMvc
            .post("/v1/admin/content/documents") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                contentType = MediaType.APPLICATION_JSON
                content =
                    objectMapper.writeValueAsString(
                        mapOf(
                            "slug" to "blocked-$suffix",
                            "locale" to "fa-IR",
                            "title" to "نمونه",
                            "summary" to "غیرپزشکی",
                            "bodyRichtext" to "fixture",
                            "contentType" to "article",
                            "lifeStage" to "cycle",
                            "medicalRiskLevel" to "general_education",
                            "tags" to emptyList<String>(),
                            "sourceIds" to emptyList<String>(),
                        ),
                    )
            }.andExpect {
                status { isForbidden() }
            }
    }
}
