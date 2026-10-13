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
class M17aPlaceholderProductionIntegrationTest(
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
    fun placeholderProgramCannotPublishInProduction() {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val body =
            mapOf(
                "slug" to "program-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "برنامه",
                "contentType" to ContentTypes.PROGRAM,
                "medicalRiskLevel" to "general_education",
                "sourceIds" to listOf(sourceId),
                "typePayload" to
                    mapOf(
                        "placeholder" to true,
                        "programType" to "breathing",
                        "safetyNotice" to "نمونه",
                        "contraindications" to "نمونه",
                        "sessions" to
                            listOf(
                                mapOf(
                                    "id" to "s1",
                                    "title" to "جلسه",
                                    "order" to 1,
                                    "steps" to listOf(mapOf("instruction" to "نمونه")),
                                ),
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
        val versionId = objectMapper.readTree(created).get("versionId").asText()
        WellnessContentTestSupport.workflow(
            WellnessContentTestSupport.WorkflowCall(
                mockMvc,
                objectMapper,
                versionId,
                "submit_medical_review",
                tokens.editorToken,
            ),
        )
        WellnessContentTestSupport.workflow(
            WellnessContentTestSupport.WorkflowCall(
                mockMvc,
                objectMapper,
                versionId,
                "approve_medical",
                tokens.medicalToken,
            ),
        )
        WellnessContentTestSupport.workflow(
            WellnessContentTestSupport.WorkflowCall(
                mockMvc,
                objectMapper,
                versionId,
                "approve_editorial",
                tokens.editorToken,
            ),
        )
        mockMvc
            .post("/v1/admin/content/versions/$versionId/workflow/publish") {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${tokens.editorToken}")
                contentType = MediaType.APPLICATION_JSON
                content = "{}"
            }.andExpect {
                status { isConflict() }
                jsonPath("$.code") { value("placeholder_publish_not_allowed") }
            }
    }

    @Test
    fun misPublishedPlaceholderProgramNotServedFromPublicApiInProduction() {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val body =
            mapOf(
                "slug" to "program-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "برنامه",
                "contentType" to ContentTypes.PROGRAM,
                "medicalRiskLevel" to "general_education",
                "sourceIds" to listOf(sourceId),
                "typePayload" to
                    mapOf(
                        "placeholder" to true,
                        "programType" to "breathing",
                        "safetyNotice" to "نمونه",
                        "contraindications" to "نمونه",
                        "sessions" to
                            listOf(
                                mapOf(
                                    "id" to "s1",
                                    "title" to "جلسه",
                                    "order" to 1,
                                    "steps" to listOf(mapOf("instruction" to "نمونه")),
                                ),
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

        mockMvc.get("/v1/content/programs/$documentId").andExpect { status { isNotFound() } }
        mockMvc.get("/v1/content/programs").andExpect {
            status { isOk() }
            jsonPath("$.items[?(@.id == '$documentId')]") { doesNotExist() }
        }
    }
}
