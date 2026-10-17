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
class M18bGatekeeperIntegrationTest(
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
    fun publishedWarningSignsIsServedWithThresholdsFromPayload() {
        val documentId = createWarningSigns(publish = true)
        mockMvc.get("/v1/content/warning-signs/$documentId").andExpect {
            status { isOk() }
            jsonPath("$.placeholder") { value(true) }
            jsonPath("$.bpThresholds.elevatedSystolic") { value(140) }
            jsonPath("$.urgentContactClinicianLabel") { value("فوراً با مراقب تماس بگیرید") }
            jsonPath("$.signs[0].title") { value("علامت نمونه") }
        }
    }

    @Test
    fun draftWarningSignsNotServedPublicly() {
        val documentId = createWarningSigns(publish = false)
        mockMvc.get("/v1/content/warning-signs/$documentId").andExpect { status { isNotFound() } }
    }

    @Test
    fun companionDeliveryMatrixForWarningSigns() {
        val draft = createWarningSigns(publish = false)
        assertExcludedFromPublicDelivery(draft, "/v1/content/warning-signs")

        val expiredCreated = createWarningSignsDocument()
        publishVersion(expiredCreated.versionId, thenWithdraw = false)
        val expiredPublishedVersionId =
            documentRepository
                .findById(UUID.fromString(expiredCreated.documentId))
                .orElseThrow()
                .publishedVersionId!!
                .toString()
        applyEffectiveWindow(
            expiredPublishedVersionId,
            effectiveFrom = null,
            effectiveTo = Instant.now().minusSeconds(60).toString(),
        )
        assertExcludedFromPublicDelivery(expiredCreated.documentId, "/v1/content/warning-signs")

        val futureCreated = createWarningSignsDocument()
        applyEffectiveWindow(
            futureCreated.versionId,
            effectiveFrom = Instant.now().plus(1, ChronoUnit.DAYS).toString(),
            effectiveTo = null,
        )
        publishVersion(futureCreated.versionId, thenWithdraw = false)
        assertExcludedFromPublicDelivery(futureCreated.documentId, "/v1/content/warning-signs")

        val withdrawn = createWarningSigns(publish = true, thenWithdraw = true)
        assertExcludedFromPublicDelivery(withdrawn, "/v1/content/warning-signs")

        val published = createWarningSigns(publish = true)
        assertPublishedAccessible(published, "/v1/content/warning-signs")
    }

    @Test
    fun publishWarningSignsWithoutMedicalSignoffReturns409() {
        val created = createWarningSignsDocument()
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
    fun warningSignsExcludedFromArticleAndSearchEndpoints() {
        val warningId = createWarningSigns(publish = true)
        mockMvc.get("/v1/content/articles/$warningId").andExpect { status { isNotFound() } }
        mockMvc.get("/v1/content/search").andExpect {
            status { isOk() }
            jsonPath("$.items[?(@.id == '$warningId')]") { doesNotExist() }
        }
        mockMvc.get("/v1/content/warning-signs/$warningId").andExpect { status { isOk() } }
    }

    @Test
    fun warningSignsWithNoneRiskRejectedOnCreate() {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val body =
            mapOf(
                "slug" to "warning-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "هشدار",
                "contentType" to ContentTypes.WARNING_SIGNS,
                "medicalRiskLevel" to "none",
                "sourceIds" to listOf(sourceId),
                "typePayload" to sampleWarningSignsPayload(),
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
    fun adminValidatorRejectsWarningSignsWithoutSigns() {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val payload = sampleWarningSignsPayload().toMutableMap()
        payload["signs"] = emptyList<Any>()
        val body =
            mapOf(
                "slug" to "warning-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "هشدار",
                "contentType" to ContentTypes.WARNING_SIGNS,
                "medicalRiskLevel" to "general_education",
                "sourceIds" to listOf(sourceId),
                "typePayload" to payload,
            )
        mockMvc
            .post("/v1/admin/content/documents") {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${tokens.editorToken}")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(body)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("warning_signs_required") }
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

    private fun createWarningSigns(
        publish: Boolean,
        thenWithdraw: Boolean = false,
    ): String {
        val created = createWarningSignsDocument()
        if (publish) {
            publishVersion(created.versionId, thenWithdraw)
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
        return createDocument(body)
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
            "urgentEscalationTitle" to "مراقبت فوری",
            "urgentEscalationBody" to "در صورت علائم شدید فوراً اقدام کنید",
            "urgentContactClinicianLabel" to "فوراً با مراقب تماس بگیرید",
            "bpThresholds" to
                mapOf(
                    "elevatedSystolic" to 140,
                    "elevatedDiastolic" to 90,
                    "urgentSystolic" to 160,
                    "urgentDiastolic" to 110,
                ),
        )
}
