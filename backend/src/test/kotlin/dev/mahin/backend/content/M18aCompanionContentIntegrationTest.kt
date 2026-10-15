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
class M18aCompanionContentIntegrationTest(
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
    fun publishedCareScheduleIsServedWithItems() {
        val documentId = createAndPublishCareSchedule()
        mockMvc.get("/v1/content/care-schedules/$documentId").andExpect {
            status { isOk() }
            jsonPath("$.placeholder") { value(true) }
            jsonPath("$.items[0].title") { value("آزمایش نمونه") }
            jsonPath("$.regionContext") { value("iran") }
        }
    }

    @Test
    fun publishedChecklistTemplateIsServedWithItems() {
        val documentId = createAndPublishChecklist()
        mockMvc.get("/v1/content/checklist-templates/$documentId").andExpect {
            status { isOk() }
            jsonPath("$.checklistKind") { value("hospital_bag") }
            jsonPath("$.items[0].label") { value("لیست نمونه") }
        }
    }

    @Test
    fun draftCareScheduleNotServedPublicly() {
        val documentId = createCareSchedule(publish = false)
        mockMvc.get("/v1/content/care-schedules/$documentId").andExpect { status { isNotFound() } }
    }

    private fun createAndPublishCareSchedule(): String {
        val documentId = createCareSchedule(publish = true)
        return documentId
    }

    private fun createCareSchedule(publish: Boolean): String {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val body =
            mapOf(
                "slug" to "care-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "زمان‌بندی مراقبت",
                "contentType" to ContentTypes.CARE_SCHEDULE,
                "medicalRiskLevel" to "general_education",
                "sourceIds" to listOf(sourceId),
                "typePayload" to sampleCareSchedulePayload(),
            )
        val created = createDocument(body)
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

    private fun createAndPublishChecklist(): String = createChecklist(publish = true)

    private fun createChecklist(publish: Boolean): String {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val body =
            mapOf(
                "slug" to "checklist-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "لیست بیمارستان",
                "contentType" to ContentTypes.CHECKLIST_TEMPLATE,
                "medicalRiskLevel" to "general_education",
                "sourceIds" to listOf(sourceId),
                "typePayload" to sampleChecklistPayload(),
            )
        val created = createDocument(body)
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

    private data class CreatedDocument(
        val documentId: String,
        val versionId: String,
    )

    private fun createDocument(body: Map<String, Any>): CreatedDocument {
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

    private fun sampleCareSchedulePayload(): Map<String, Any> =
        mapOf(
            "placeholder" to true,
            "regionContext" to "iran",
            "items" to
                listOf(
                    mapOf(
                        "id" to "item-1",
                        "title" to "آزمایش نمونه",
                        "category" to "test",
                        "weekStart" to 8,
                        "weekEnd" to 10,
                        "description" to "نمونه — غیرپزشکی",
                    ),
                ),
        )

    private fun sampleChecklistPayload(): Map<String, Any> =
        mapOf(
            "placeholder" to true,
            "checklistKind" to "hospital_bag",
            "sections" to listOf("مدارک"),
            "items" to
                listOf(
                    mapOf(
                        "id" to "c1",
                        "label" to "لیست نمونه",
                        "section" to "مدارک",
                        "order" to 1,
                    ),
                ),
        )
}
