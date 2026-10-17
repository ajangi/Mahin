package dev.mahin.backend.content

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.cms.CmsAuthService
import dev.mahin.backend.content.persistence.ContentDocumentRepository
import dev.mahin.backend.content.persistence.ContentVersionRepository
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = ["mahin.environment=production"])
class M18bPlaceholderProductionIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val cmsAuthService: CmsAuthService,
    @Autowired private val documentRepository: ContentDocumentRepository,
    @Autowired private val versionRepository: ContentVersionRepository,
) {
    private lateinit var tokens: WellnessContentTestSupport.StaffTokens

    @BeforeEach
    fun seedStaff() {
        tokens = WellnessContentTestSupport.seedStaff(mockMvc, objectMapper, cmsAuthService)
    }

    @Test
    fun misPublishedPlaceholderWarningSignsNotServedFromPublicApiInProduction() {
        val published = publishPlaceholderWarningSigns()
        mockMvc.get("/v1/content/warning-signs/${published.documentId}").andExpect { status { isNotFound() } }
        mockMvc.get("/v1/content/warning-signs/by-slug/${published.slug}").andExpect { status { isNotFound() } }
        mockMvc.get("/v1/content/warning-signs").andExpect {
            status { isOk() }
            jsonPath("$.items[?(@.id == '${published.documentId}')]") { doesNotExist() }
        }
        mockMvc.get("/v1/content/articles/${published.documentId}").andExpect { status { isNotFound() } }
        mockMvc.get("/v1/content/search").andExpect {
            status { isOk() }
            jsonPath("$.items[?(@.id == '${published.documentId}')]") { doesNotExist() }
        }
    }

    private fun publishPlaceholderWarningSigns(): PublishedPlaceholder {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val slug = "warning-${UUID.randomUUID()}"
        val body =
            mapOf(
                "slug" to slug,
                "locale" to "fa-IR",
                "title" to "هشدار",
                "contentType" to ContentTypes.WARNING_SIGNS,
                "medicalRiskLevel" to "general_education",
                "sourceIds" to listOf(sourceId),
                "typePayload" to
                    mapOf(
                        "placeholder" to true,
                        "introTitle" to "نمونه",
                        "introBody" to "نمونه",
                        "signs" to
                            listOf(
                                mapOf(
                                    "id" to "s1",
                                    "title" to "نمونه",
                                    "summary" to "نمونه",
                                ),
                            ),
                        "escalationTitle" to "نمونه",
                        "escalationBody" to "نمونه",
                        "contactClinicianLabel" to "نمونه",
                        "urgentEscalationTitle" to "نمونه",
                        "urgentEscalationBody" to "نمونه",
                        "urgentContactClinicianLabel" to "نمونه",
                        "bpThresholds" to
                            mapOf(
                                "elevatedSystolic" to 140,
                                "elevatedDiastolic" to 90,
                                "urgentSystolic" to 160,
                                "urgentDiastolic" to 110,
                            ),
                    ),
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
        val versionId = UUID.fromString(objectMapper.readTree(created).get("versionId").asText())
        val documentId = UUID.fromString(objectMapper.readTree(created).get("documentId").asText())
        val version = versionRepository.findById(versionId).orElseThrow()
        val document = documentRepository.findById(documentId).orElseThrow()
        version.status = "published"
        versionRepository.save(version)
        document.publishedVersionId = version.id
        documentRepository.save(document)
        return PublishedPlaceholder(documentId = documentId.toString(), slug = document.slug)
    }

    private data class PublishedPlaceholder(
        val documentId: String,
        val slug: String,
    )
}
