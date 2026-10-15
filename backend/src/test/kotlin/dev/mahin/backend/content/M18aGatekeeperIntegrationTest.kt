package dev.mahin.backend.content

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.cms.CmsAuthService
import dev.mahin.backend.content.persistence.ContentDocumentRepository
import dev.mahin.backend.content.persistence.ContentVersionRepository
import java.time.Instant
import java.time.temporal.ChronoUnit
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
class M18aGatekeeperIntegrationTest(
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
    fun publishedCareScheduleIsServedWithItems() {
        val documentId = createCareSchedule(publish = true)
        mockMvc.get("/v1/content/care-schedules/$documentId").andExpect {
            status { isOk() }
            jsonPath("$.placeholder") { value(true) }
            jsonPath("$.items[0].title") { value("آزمایش نمونه") }
            jsonPath("$.regionContext") { value("iran") }
        }
    }

    @Test
    fun publishedChecklistTemplateIsServedWithItems() {
        val documentId = createChecklist(publish = true)
        mockMvc.get("/v1/content/checklist-templates/$documentId").andExpect {
            status { isOk() }
            jsonPath("$.checklistKind") { value("hospital_bag") }
            jsonPath("$.items[0].label") { value("لیست نمونه") }
        }
    }

    @Test
    fun draftCareScheduleAndChecklistNotServedPublicly() {
        val careId = createCareSchedule(publish = false)
        val checklistId = createChecklist(publish = false)
        mockMvc.get("/v1/content/care-schedules/$careId").andExpect { status { isNotFound() } }
        mockMvc.get("/v1/content/checklist-templates/$checklistId").andExpect { status { isNotFound() } }
    }

    @Test
    fun companionDeliveryMatrixForCareScheduleAndChecklist() {
        val draftCare = createCareSchedule(publish = false)
        val draftChecklist = createChecklist(publish = false)
        assertExcludedFromPublicDelivery(draftCare, "/v1/content/care-schedules")
        assertExcludedFromPublicDelivery(draftChecklist, "/v1/content/checklist-templates")

        val expiredCareCreated = createCareScheduleDocument(publish = false)
        publishVersion(expiredCareCreated.versionId, thenWithdraw = false)
        val expiredPublishedVersionId =
            documentRepository
                .findById(UUID.fromString(expiredCareCreated.documentId))
                .orElseThrow()
                .publishedVersionId!!
                .toString()
        applyEffectiveWindow(
            expiredPublishedVersionId,
            effectiveFrom = null,
            effectiveTo = Instant.now().minusSeconds(60).toString(),
        )
        assertExcludedFromPublicDelivery(expiredCareCreated.documentId, "/v1/content/care-schedules")

        val futureChecklistCreated = createChecklistDocument(publish = false)
        applyEffectiveWindow(
            futureChecklistCreated.versionId,
            effectiveFrom = Instant.now().plus(1, ChronoUnit.DAYS).toString(),
            effectiveTo = null,
        )
        publishVersion(futureChecklistCreated.versionId, thenWithdraw = false)
        assertExcludedFromPublicDelivery(futureChecklistCreated.documentId, "/v1/content/checklist-templates")

        val withdrawnCare = createCareSchedule(publish = true, thenWithdraw = true)
        assertExcludedFromPublicDelivery(withdrawnCare, "/v1/content/care-schedules")

        val publishedChecklist = createChecklist(publish = true)
        assertPublishedAccessible(publishedChecklist, "/v1/content/checklist-templates")

        val publishedCare = createCareSchedule(publish = true)
        assertPublishedAccessible(publishedCare, "/v1/content/care-schedules")
    }

    @Test
    fun gestationalWeekFilterIncludesMatchingScheduleAndExcludesNonMatching() {
        val documentId = createCareSchedule(publish = true)
        mockMvc
            .get("/v1/content/care-schedules") {
                param("gestationalWeek", "9")
            }.andExpect {
                status { isOk() }
                jsonPath("$.items[?(@.id == '$documentId')].id") { value(documentId) }
            }
        mockMvc
            .get("/v1/content/care-schedules") {
                param("gestationalWeek", "20")
            }.andExpect {
                status { isOk() }
                jsonPath("$.items[?(@.id == '$documentId')]") { doesNotExist() }
            }
    }

    @Test
    fun invalidGestationalWeekAndChecklistKindReturn400() {
        mockMvc
            .get("/v1/content/care-schedules") {
                param("gestationalWeek", "99")
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("invalid_gestational_week") }
            }
        mockMvc
            .get("/v1/content/checklist-templates") {
                param("checklistKind", "unknown")
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("invalid_checklist_kind") }
            }
    }

    @Test
    fun careScheduleWithNoneRiskRejectedOnCreate() {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val body =
            mapOf(
                "slug" to "care-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "زمان‌بندی",
                "contentType" to ContentTypes.CARE_SCHEDULE,
                "medicalRiskLevel" to "none",
                "sourceIds" to listOf(sourceId),
                "typePayload" to sampleCareSchedulePayload(),
            )
        mockMvc
            .post("/v1/admin/content/documents") {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${tokens.editorToken}")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(body)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("wellness_requires_clinical_review") }
            }
    }

    @Test
    fun publishCareScheduleWithoutMedicalSignoffReturns409() {
        val created = createCareScheduleDocument(publish = false)
        val version = versionRepository.findById(UUID.fromString(created.versionId)).orElseThrow()
        version.status = "approved"
        version.clinicalReviewedAt = null
        versionRepository.save(version)
        mockMvc
            .post("/v1/admin/content/versions/${created.versionId}/workflow/publish") {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${tokens.editorToken}")
                contentType = MediaType.APPLICATION_JSON
                content = "{}"
            }.andExpect {
                status { isConflict() }
                jsonPath("$.code") { value("clinical_review_required") }
            }
    }

    @Test
    fun companionTypesExcludedFromArticleAndSearchEndpoints() {
        val careId = createCareSchedule(publish = true)
        val checklistId = createChecklist(publish = true)
        val weekId = createPregnancyWeek(publish = true, week = 14)
        mockMvc.get("/v1/content/articles/$careId").andExpect { status { isNotFound() } }
        mockMvc.get("/v1/content/articles/$checklistId").andExpect { status { isNotFound() } }
        mockMvc.get("/v1/content/articles/$weekId").andExpect { status { isNotFound() } }
        mockMvc.get("/v1/content/search").andExpect {
            status { isOk() }
            jsonPath("$.items[?(@.id == '$careId')]") { doesNotExist() }
            jsonPath("$.items[?(@.id == '$checklistId')]") { doesNotExist() }
            jsonPath("$.items[?(@.id == '$weekId')]") { doesNotExist() }
        }
        mockMvc.get("/v1/content/pregnancy/weeks/14").andExpect { status { isOk() } }
    }

    @Test
    fun adminValidatorRejectsCareScheduleWithoutItems() {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val body =
            mapOf(
                "slug" to "care-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "زمان‌بندی",
                "contentType" to ContentTypes.CARE_SCHEDULE,
                "medicalRiskLevel" to "general_education",
                "sourceIds" to listOf(sourceId),
                "typePayload" to mapOf("placeholder" to true, "regionContext" to "iran", "items" to emptyList<Any>()),
            )
        mockMvc
            .post("/v1/admin/content/documents") {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${tokens.editorToken}")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(body)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("care_schedule_items_required") }
            }
    }

    @Test
    fun adminValidatorRejectsOversizedCompanionPayload() {
        val padding = "x".repeat(ContentTypePayloadLimits.MAX_PAYLOAD_BYTES)
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val body =
            mapOf(
                "slug" to "checklist-${UUID.randomUUID()}",
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
                                    "label" to padding,
                                    "section" to "مدارک",
                                    "order" to 1,
                                ),
                            ),
                    ),
            )
        mockMvc
            .post("/v1/admin/content/documents") {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${tokens.editorToken}")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(body)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("type_payload_too_large") }
            }
    }

    private fun assertExcludedFromPublicDelivery(
        id: String,
        basePath: String,
    ) {
        val slug = slugFor(id)
        mockMvc.get("$basePath/$id").andExpect { status { isNotFound() } }
        mockMvc.get("$basePath/by-slug/$slug").andExpect { status { isNotFound() } }
        mockMvc.get(basePath).andExpect {
            status { isOk() }
            jsonPath("$.items[?(@.id == '$id')]") { doesNotExist() }
        }
    }

    private fun assertPublishedAccessible(
        id: String,
        basePath: String,
    ) {
        val slug = slugFor(id)
        mockMvc.get("$basePath/$id").andExpect { status { isOk() } }
        mockMvc.get("$basePath/by-slug/$slug").andExpect { status { isOk() } }
        mockMvc.get(basePath).andExpect {
            status { isOk() }
            jsonPath("$.items[?(@.id == '$id')].id") { value(id) }
        }
    }

    private fun slugFor(documentId: String): String =
        documentRepository.findById(UUID.fromString(documentId)).orElseThrow().slug

    private fun createCareSchedule(
        publish: Boolean,
        effectiveFrom: String? = null,
        effectiveTo: String? = null,
        thenWithdraw: Boolean = false,
    ): String {
        val created = createCareScheduleDocument(publish = false)
        applyEffectiveWindow(created.versionId, effectiveFrom, effectiveTo)
        if (publish) {
            publishVersion(created.versionId, thenWithdraw)
        }
        return created.documentId
    }

    private fun createChecklist(
        publish: Boolean,
        effectiveFrom: String? = null,
        effectiveTo: String? = null,
        thenWithdraw: Boolean = false,
    ): String {
        val created = createChecklistDocument(publish = false)
        applyEffectiveWindow(created.versionId, effectiveFrom, effectiveTo)
        if (publish) {
            publishVersion(created.versionId, thenWithdraw)
        }
        return created.documentId
    }

    private fun createPregnancyWeek(
        publish: Boolean,
        week: Int,
    ): String {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val body =
            mapOf(
                "slug" to "week-$week-${UUID.randomUUID()}",
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
        val created = createDocument(body)
        if (publish) {
            publishVersion(created.versionId, thenWithdraw = false)
        }
        return created.documentId
    }

    private fun applyEffectiveWindow(
        versionId: String,
        effectiveFrom: String?,
        effectiveTo: String?,
    ) {
        if (effectiveFrom == null && effectiveTo == null) return
        val version = versionRepository.findById(UUID.fromString(versionId)).orElseThrow()
        if (effectiveFrom != null) {
            version.effectiveFrom = Instant.parse(effectiveFrom)
        }
        if (effectiveTo != null) {
            version.effectiveTo = Instant.parse(effectiveTo)
        }
        versionRepository.save(version)
    }

    private fun publishVersion(
        versionId: String,
        thenWithdraw: Boolean,
    ) {
        WellnessContentTestSupport.publishWellness(
            mockMvc,
            objectMapper,
            versionId,
            tokens.editorToken,
            tokens.medicalToken,
        )
        if (thenWithdraw) {
            WellnessContentTestSupport.workflow(
                WellnessContentTestSupport.WorkflowCall(
                    mockMvc,
                    objectMapper,
                    versionId,
                    "retire",
                    tokens.editorToken,
                ),
            )
        }
    }

    private fun createCareScheduleDocument(publish: Boolean): CreatedDocument {
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
        return createDocument(body)
    }

    private fun createChecklistDocument(publish: Boolean): CreatedDocument {
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
        return createDocument(body)
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
