package dev.mahin.backend.content

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.cms.CmsAuthService
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class M18bGatekeeperIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val cmsAuthService: CmsAuthService,
) {
    private lateinit var tokens: WellnessContentTestSupport.StaffTokens

    @BeforeEach
    fun seedStaff() {
        tokens = WellnessContentTestSupport.seedStaff(mockMvc, objectMapper, cmsAuthService)
    }

    @Test
    fun publishedWarningSignsIsServedWithThresholdsFromPayload() {
        val documentId = createWarningSigns(publish = true)
        mockMvc.get("/v1/content/warning-signs/$documentId").andExpect {
            status { isOk() }
            jsonPath("$.placeholder") { value(true) }
            jsonPath("$.bpThresholds.elevatedSystolic") { value(140) }
            jsonPath("$.signs[0].title") { value("علامت نمونه") }
        }
    }

    @Test
    fun draftWarningSignsNotServedPublicly() {
        val documentId = createWarningSigns(publish = false)
        mockMvc.get("/v1/content/warning-signs/$documentId").andExpect { status { isNotFound() } }
    }

    private fun createWarningSigns(publish: Boolean): String {
        val created = createWarningSignsDocument()
        if (publish) {
            WellnessContentTestSupport.publishWellness(
                mockMvc,
                objectMapper,
                created.versionId,
                tokens.editorToken,
                tokens.medicalToken,
            )
        }
        return created.documentId
    }

    private fun createWarningSignsDocument(): CreatedDocument {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val body =
            mapOf(
                "slug" to "warning-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "هشدارهای بارداری",
                "contentType" to ContentTypes.WARNING_SIGNS,
                "medicalRiskLevel" to "general_education",
                "sourceIds" to listOf(sourceId),
                "typePayload" to sampleWarningSignsPayload(),
            )
        val created =
            mockMvc
                .post("/v1/admin/content/documents") {
                    header(HttpHeaders.AUTHORIZATION, "Bearer ${tokens.editorToken}")
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(body)
                }.andExpect { status { isOk() } }
                .andReturn()
                .response.contentAsString
        val tree = objectMapper.readTree(created)
        return CreatedDocument(
            documentId = tree.get("documentId").asText(),
            versionId = tree.get("versionId").asText(),
        )
    }

    private data class CreatedDocument(
        val documentId: String,
        val versionId: String,
    )

    private fun sampleWarningSignsPayload(): Map<String, Any> =
        mapOf(
            "placeholder" to true,
            "introTitle" to "مقدمه نمونه",
            "introBody" to "متن آموزشی نمونه — نیازمند بازبینی بالینی",
            "signs" to
                listOf(
                    mapOf(
                        "id" to "s1",
                        "title" to "علامت نمونه",
                        "summary" to "توضیح نمونه",
                    ),
                ),
            "escalationTitle" to "ارتقا",
            "escalationBody" to "در صورت نگرانی با مراقب تماس بگیرید",
            "contactClinicianLabel" to "با پزشک یا ماما تماس بگیرید",
            "bpThresholds" to
                mapOf(
                    "elevatedSystolic" to 140,
                    "elevatedDiastolic" to 90,
                    "urgentSystolic" to 160,
                    "urgentDiastolic" to 110,
                ),
        )
}
