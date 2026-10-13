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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class M17aGatekeeperIntegrationTest(
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
    fun expiredTipNotServedFromArticleEndpointOrSearch() {
        val documentId =
            createDailyTip(
                publish = true,
                effectiveTo = Instant.now().minus(1, ChronoUnit.HOURS).toString(),
            )
        mockMvc.get("/v1/content/articles/$documentId").andExpect { status { isNotFound() } }
        mockMvc.get("/v1/content/search").andExpect {
            status { isOk() }
            jsonPath("$.items[?(@.id == '$documentId')]") { doesNotExist() }
        }
    }

    @Test
    fun futureArticleEffectiveFromIsNotServedFromArticleEndpoint() {
        val articleId = createArticle(publish = true)
        val document = documentRepository.findById(UUID.fromString(articleId)).orElseThrow()
        val version = versionRepository.findById(document.publishedVersionId!!).orElseThrow()
        version.effectiveFrom = Instant.now().plus(2, ChronoUnit.DAYS)
        versionRepository.save(version)
        mockMvc.get("/v1/content/articles/$articleId").andExpect { status { isNotFound() } }
    }

    @Test
    fun programWithNoneRiskRejectedOnCreate() {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val body =
            mapOf(
                "slug" to "program-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "برنامه",
                "contentType" to ContentTypes.PROGRAM,
                "medicalRiskLevel" to "none",
                "sourceIds" to listOf(sourceId),
                "typePayload" to minimalProgramPayload(),
            )
        mockMvc
            .post("/v1/admin/content/documents") {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${tokens.editorToken}")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(body)
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("program_requires_clinical_review") }
            }
    }

    @Test
    fun publishProgramWithoutMedicalSignoffReturnsClinicalReviewRequired() {
        val versionId = createProgramDraft()
        val version = versionRepository.findById(UUID.fromString(versionId)).orElseThrow()
        version.status = "approved"
        version.clinicalReviewedAt = null
        versionRepository.save(version)
        mockMvc
            .post("/v1/admin/content/versions/$versionId/workflow/publish") {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${tokens.editorToken}")
                contentType = MediaType.APPLICATION_JSON
                content = "{}"
            }.andExpect {
                status { isConflict() }
                jsonPath("$.code") { value("clinical_review_required") }
            }
    }

    @Test
    fun approveEditorialWithoutMedicalReviewIsRejected() {
        val versionId = createProgramDraft()
        WellnessContentTestSupport.workflow(
            WellnessContentTestSupport.WorkflowCall(
                mockMvc,
                objectMapper,
                versionId,
                "submit_medical_review",
                tokens.editorToken,
            ),
        )
        mockMvc
            .post("/v1/admin/content/versions/$versionId/workflow/approve_editorial") {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${tokens.editorToken}")
                contentType = MediaType.APPLICATION_JSON
                content = "{}"
            }.andExpect { status { isConflict() } }
    }

    @Test
    fun clinicalReviewerUsesMedicalStaffDisplayName() {
        val documentId = createDailyTip(publish = true)
        mockMvc.get("/v1/content/daily-tips/$documentId").andExpect {
            status { isOk() }
            jsonPath("$.clinicalReviewer") { value(tokens.medicalDisplayName) }
        }
    }

    @Test
    fun wellnessDeliveryMatrixForTipProgramCollection() {
        val tipId = createDailyTip(publish = false)
        val programId = createProgram(publish = false)
        val collectionId = createCollection(publish = false, articleIds = emptyList())
        assertNotFoundAll(tipId, "/v1/content/daily-tips")
        assertNotFoundAll(programId, "/v1/content/programs")
        assertNotFoundAll(collectionId, "/v1/content/collections")

        val expiredTip =
            createDailyTip(
                publish = true,
                effectiveTo = Instant.now().minusSeconds(60).toString(),
            )
        assertAbsentFromList(expiredTip, "/v1/content/daily-tips")

        val futureTip =
            createDailyTip(
                publish = true,
                effectiveFrom = Instant.now().plus(1, ChronoUnit.DAYS).toString(),
            )
        assertAbsentFromList(futureTip, "/v1/content/daily-tips")

        val atBoundaryTip =
            createDailyTip(
                publish = true,
                effectiveTo = Instant.now().toString(),
            )
        assertAbsentFromList(atBoundaryTip, "/v1/content/daily-tips")

        val publishedTip = createDailyTip(publish = true)
        mockMvc.get("/v1/content/daily-tips/$publishedTip").andExpect { status { isOk() } }
        mockMvc.get("/v1/content/daily-tips/by-slug/${slugFor(publishedTip)}").andExpect { status { isOk() } }
    }

    @Test
    fun collectionExcludesDraftExpiredWithdrawnAndWrongLocaleArticles() {
        val good = createArticle(publish = true, locale = "fa-IR")
        val draft = createArticle(publish = false, locale = "fa-IR")
        val expired =
            createArticle(publish = true, locale = "fa-IR", effectiveTo = Instant.now().minus(1, ChronoUnit.HOURS))
        val withdrawn = createArticle(publish = true, locale = "fa-IR", thenWithdraw = true)
        val wrongLocale = createArticle(publish = true, locale = "en-US")
        val collectionId =
            createCollection(
                publish = true,
                articleIds = listOf(good, draft, expired, withdrawn, wrongLocale),
            )
        mockMvc.get("/v1/content/collections/$collectionId").andExpect {
            status { isOk() }
            jsonPath("$.articles.length()") { value(1) }
            jsonPath("$.articles[0].id") { value(good) }
        }
    }

    @Test
    fun invalidDailyTipQueryReturns400() {
        mockMvc
            .get("/v1/content/daily-tips") {
                param("pregnancyWeek", "99")
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("invalid_daily_tip_pregnancy_week") }
            }
    }

    private fun assertNotFoundAll(
        id: String,
        basePath: String,
    ) {
        mockMvc.get("$basePath/$id").andExpect { status { isNotFound() } }
        mockMvc.get(basePath).andExpect {
            status { isOk() }
            jsonPath("$.items[?(@.id == '$id')]") { doesNotExist() }
        }
    }

    private fun assertAbsentFromList(
        id: String,
        basePath: String,
    ) {
        mockMvc.get(basePath).andExpect {
            status { isOk() }
            jsonPath("$.items[?(@.id == '$id')]") { doesNotExist() }
        }
        mockMvc.get("$basePath/$id").andExpect { status { isNotFound() } }
    }

    private fun slugFor(documentId: String): String =
        documentRepository.findById(UUID.fromString(documentId)).orElseThrow().slug

    private fun createDailyTip(
        publish: Boolean,
        effectiveFrom: String? = null,
        effectiveTo: String? = null,
    ): String {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val body =
            mapOf(
                "slug" to "tip-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "نکتهٔ نمونه",
                "bodyRichtext" to "متن",
                "contentType" to ContentTypes.DAILY_TIP,
                "medicalRiskLevel" to "general_education",
                "sourceIds" to listOf(sourceId),
                "typePayload" to
                    mapOf(
                        "placeholder" to true,
                        "targeting" to mapOf("modes" to listOf("cycle")),
                    ),
                "effectiveFrom" to effectiveFrom,
                "effectiveTo" to effectiveTo,
            ).filterValues { it != null }
        return createAndMaybePublish(body, publish)
    }

    private fun createProgramDraft(): String {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val body =
            mapOf(
                "slug" to "program-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "برنامه",
                "contentType" to ContentTypes.PROGRAM,
                "medicalRiskLevel" to "general_education",
                "sourceIds" to listOf(sourceId),
                "typePayload" to minimalProgramPayload(),
            )
        val created = createDocument(body)
        return created.get("versionId").asText()
    }

    private fun createProgram(publish: Boolean): String {
        val versionId = createProgramDraft()
        val documentId = versionIdToDocumentId(versionId)
        if (publish) {
            WellnessContentTestSupport.publishWellness(
                mockMvc,
                objectMapper,
                versionId,
                tokens.editorToken,
                tokens.medicalToken,
            )
        }
        return documentId
    }

    private fun createCollection(
        publish: Boolean,
        articleIds: List<String>,
    ): String {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val body =
            mapOf(
                "slug" to "collection-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "مجموعه",
                "contentType" to ContentTypes.COLLECTION,
                "medicalRiskLevel" to "none",
                "sourceIds" to listOf(sourceId),
                "typePayload" to
                    mapOf(
                        "placeholder" to true,
                        "category" to "cycle",
                        "articleDocumentIds" to articleIds,
                    ),
            )
        return createAndMaybePublish(body, publish)
    }

    private fun createArticle(
        publish: Boolean,
        locale: String = "fa-IR",
        effectiveTo: Instant? = null,
        thenWithdraw: Boolean = false,
    ): String {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val body =
            mapOf(
                "slug" to "article-${UUID.randomUUID()}",
                "locale" to locale,
                "title" to "مقاله",
                "bodyRichtext" to "متن",
                "contentType" to "article",
                "medicalRiskLevel" to "none",
                "sourceIds" to listOf(sourceId),
            )
        val created = createDocument(body)
        val versionId = created.get("versionId").asText()
        val documentId = created.get("documentId").asText()
        if (publish) {
            WellnessContentTestSupport.publishWellness(
                mockMvc,
                objectMapper,
                versionId,
                tokens.editorToken,
                tokens.medicalToken,
            )
            if (effectiveTo != null) {
                val document = documentRepository.findById(UUID.fromString(documentId)).orElseThrow()
                val version = versionRepository.findById(document.publishedVersionId!!).orElseThrow()
                version.effectiveTo = effectiveTo
                versionRepository.save(version)
            }
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
        return documentId
    }

    private fun minimalProgramPayload(): Map<String, Any> =
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
        )

    private fun createAndMaybePublish(
        body: Map<String, Any?>,
        publish: Boolean,
    ): String {
        val created = createDocument(body)
        val versionId = created.get("versionId").asText()
        val documentId = created.get("documentId").asText()
        if (publish) {
            WellnessContentTestSupport.publishWellness(
                mockMvc,
                objectMapper,
                versionId,
                tokens.editorToken,
                tokens.medicalToken,
            )
        }
        return documentId
    }

    private fun createDocument(body: Map<String, Any?>): com.fasterxml.jackson.databind.JsonNode {
        val response =
            mockMvc
                .post("/v1/admin/content/documents") {
                    header(HttpHeaders.AUTHORIZATION, "Bearer ${tokens.editorToken}")
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(body)
                }.andExpect { status { isOk() } }
                .andReturn()
                .response.contentAsString
        return objectMapper.readTree(response)
    }

    private fun versionIdToDocumentId(versionId: String): String =
        mockMvc
            .get("/v1/admin/content/versions/$versionId") {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${tokens.editorToken}")
            }.andReturn()
            .let { objectMapper.readTree(it.response.contentAsString).get("documentId").asText() }
}
