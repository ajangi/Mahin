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
class M18aPlaceholderProductionIntegrationTest(
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
    fun misPublishedPlaceholderCareScheduleNotServedFromPublicApiInProduction() {
        val published = publishPlaceholderCareSchedule()
        assertCompanionHiddenFromPublicApi(
            documentId = published.documentId,
            slug = published.slug,
            listPath = "/v1/content/care-schedules",
        )
    }

    @Test
    fun misPublishedPlaceholderChecklistTemplateNotServedFromPublicApiInProduction() {
        val published = publishPlaceholderChecklist()
        assertCompanionHiddenFromPublicApi(
            documentId = published.documentId,
            slug = published.slug,
            listPath = "/v1/content/checklist-templates",
        )
    }

    @Test
    fun misPublishedPlaceholderPregnancyWeekNotServedFromPublicApiInProduction() {
        val published = publishPlaceholderPregnancyWeek(week = 12)
        mockMvc.get("/v1/content/pregnancy/weeks/${published.gestationalWeek}").andExpect {
            status { isNotFound() }
        }
        mockMvc.get("/v1/content/pregnancy/weeks").andExpect {
            status { isOk() }
            jsonPath("$.weeks[?(@ == ${published.gestationalWeek})]") { doesNotExist() }
        }
        mockMvc.get("/v1/content/articles/${published.documentId}").andExpect { status { isNotFound() } }
        mockMvc.get("/v1/content/search").andExpect {
            status { isOk() }
            jsonPath("$.items[?(@.id == '${published.documentId}')]") { doesNotExist() }
        }
    }

    private fun assertCompanionHiddenFromPublicApi(
        documentId: String,
        slug: String,
        listPath: String,
    ) {
        mockMvc.get("$listPath/$documentId").andExpect { status { isNotFound() } }
        mockMvc.get("$listPath/by-slug/$slug").andExpect { status { isNotFound() } }
        mockMvc.get(listPath).andExpect {
            status { isOk() }
            jsonPath("$.items[?(@.id == '$documentId')]") { doesNotExist() }
        }
        mockMvc.get("/v1/content/articles/$documentId").andExpect { status { isNotFound() } }
        mockMvc.get("/v1/content/search").andExpect {
            status { isOk() }
            jsonPath("$.items[?(@.id == '$documentId')]") { doesNotExist() }
        }
    }

    private fun publishPlaceholderCareSchedule(): PublishedPlaceholder {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val slug = "care-${UUID.randomUUID()}"
        val body =
            mapOf(
                "slug" to slug,
                "locale" to "fa-IR",
                "title" to "زمان‌بندی",
                "contentType" to ContentTypes.CARE_SCHEDULE,
                "medicalRiskLevel" to "general_education",
                "sourceIds" to listOf(sourceId),
                "typePayload" to
                    mapOf(
                        "placeholder" to true,
                        "regionContext" to "iran",
                        "items" to
                            listOf(
                                mapOf(
                                    "id" to "i1",
                                    "title" to "نمونه",
                                    "category" to "test",
                                    "weekStart" to 8,
                                    "weekEnd" to 8,
                                ),
                            ),
                    ),
            )
        return publishDocument(body)
    }

    private fun publishPlaceholderChecklist(): PublishedPlaceholder {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val slug = "checklist-${UUID.randomUUID()}"
        val body =
            mapOf(
                "slug" to slug,
                "locale" to "fa-IR",
                "title" to "لیست",
                "contentType" to ContentTypes.CHECKLIST_TEMPLATE,
                "medicalRiskLevel" to "general_education",
                "sourceIds" to listOf(sourceId),
                "typePayload" to
                    mapOf(
                        "placeholder" to true,
                        "checklistKind" to "hospital_bag",
                        "sections" to listOf("مدارک"),
                        "items" to
                            listOf(
                                mapOf(
                                    "id" to "c1",
                                    "label" to "نمونه",
                                    "section" to "مدارک",
                                    "order" to 1,
                                ),
                            ),
                    ),
            )
        return publishDocument(body)
    }

    private fun publishPlaceholderPregnancyWeek(week: Int): PublishedPlaceholder {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val slug = "week-$week-${UUID.randomUUID()}"
        val body =
            mapOf(
                "slug" to slug,
                "locale" to "fa-IR",
                "title" to "هفته $week",
                "summary" to "خلاصه",
                "contentType" to ContentTypes.PREGNANCY_WEEK,
                "gestationalWeek" to week,
                "medicalRiskLevel" to "general_education",
                "sourceIds" to listOf(sourceId),
                "typePayload" to
                    mapOf(
                        "placeholder" to true,
                        "fetalSummary" to "نمونه",
                        "maternalSummary" to "نمونه",
                    ),
            )
        return publishDocument(body, gestationalWeek = week)
    }

    private fun publishDocument(
        body: Map<String, Any?>,
        gestationalWeek: Int? = null,
    ): PublishedPlaceholder {
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
        return PublishedPlaceholder(
            documentId = documentId.toString(),
            slug = document.slug,
            gestationalWeek = gestationalWeek,
        )
    }

    private data class PublishedPlaceholder(
        val documentId: String,
        val slug: String,
        val gestationalWeek: Int? = null,
    )
}
