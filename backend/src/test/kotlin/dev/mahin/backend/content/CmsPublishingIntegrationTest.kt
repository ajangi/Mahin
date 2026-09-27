package dev.mahin.backend.content

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.cms.CmsAuthService
import dev.mahin.backend.cms.CmsRole
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
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
class CmsPublishingIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val cmsAuthService: CmsAuthService,
) {
    private lateinit var editorToken: String
    private lateinit var medicalToken: String

    @BeforeEach
    fun seedStaff() {
        val suffix = UUID.randomUUID().toString().take(8)
        cmsAuthService.createStaff(
            email = "editor-$suffix@mahin.test",
            password = "password-12-chars",
            displayName = "Editor",
            roles = setOf(CmsRole.EDITOR),
        )
        cmsAuthService.createStaff(
            email = "medical-$suffix@mahin.test",
            password = "password-12-chars",
            displayName = "Reviewer",
            roles = setOf(CmsRole.MEDICAL_REVIEWER),
        )
        editorToken = login("editor-$suffix@mahin.test")
        medicalToken = login("medical-$suffix@mahin.test")
    }

    @Test
    fun publishAndWithdrawBumpsCatalogRevision() {
        val createBody =
            mapOf(
                "slug" to "fixture-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "نمونه غیرپزشکی",
                "summary" to "فقط برای آزمون زیرساخت CMS.",
                "bodyRichtext" to "این متن پزشکی نیست.",
                "contentType" to "article",
                "lifeStage" to "pregnancy",
                "medicalRiskLevel" to "general_education",
                "tags" to listOf("fixture"),
            )
        val created =
            mockMvc
                .post("/v1/admin/content/documents") {
                    header(HttpHeaders.AUTHORIZATION, "Bearer $editorToken")
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(createBody)
                }.andExpect { status { isOk() } }
                .andReturn()
                .response.contentAsString
        val versionId = objectMapper.readTree(created).get("versionId").asText()
        val documentId = objectMapper.readTree(created).get("documentId").asText()

        workflow(versionId, "submit_medical_review", editorToken)
        workflow(versionId, "approve_medical", medicalToken, mapOf("clinicalReviewer" to "dr.test@mahin.test"))
        workflow(versionId, "approve_editorial", editorToken)
        workflow(versionId, "publish", editorToken)

        mockMvc.get("/v1/content/articles/$documentId").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("published") }
            jsonPath("$.body") { value("این متن پزشکی نیست.") }
        }

        val revisionBefore =
            mockMvc
                .get("/v1/content/catalog-status")
                .andReturn()
                .response.contentAsString
        val revBefore = objectMapper.readTree(revisionBefore).get("publicationRevision").asLong()

        workflow(versionId, "retire", editorToken)

        mockMvc.get("/v1/content/articles/$documentId").andExpect {
            status { isOk() }
            jsonPath("$.withdrawn") { value(true) }
            jsonPath("$.body") { doesNotExist() }
        }

        val revisionAfter =
            mockMvc
                .get("/v1/content/catalog-status")
                .andReturn()
                .response.contentAsString
        val revAfter = objectMapper.readTree(revisionAfter).get("publicationRevision").asLong()
        assert(revAfter > revBefore)
    }

    private fun login(email: String): String {
        val body =
            mapOf(
                "email" to email,
                "password" to "password-12-chars",
            )
        val response =
            mockMvc
                .post("/v1/admin/auth/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(body)
                }.andExpect { status { isOk() } }
                .andReturn()
                .response.contentAsString
        return objectMapper.readTree(response).get("accessToken").asText()
    }

    private fun workflow(
        versionId: String,
        action: String,
        token: String,
        extra: Map<String, String> = emptyMap(),
    ) {
        mockMvc
            .post("/v1/admin/content/versions/$versionId/workflow/$action") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(extra)
            }.andExpect { status { isOk() } }
    }
}
