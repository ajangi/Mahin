package dev.mahin.backend.content

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.cms.CmsAuthService
import dev.mahin.backend.cms.CmsRole
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
class M17aWellnessContentIntegrationTest(
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
    fun draftDailyTipIsNotServedByPublicApi() {
        val documentId = createDailyTip(publish = false)
        mockMvc.get("/v1/content/daily-tips/$documentId").andExpect {
            status { isNotFound() }
        }
        mockMvc.get("/v1/content/daily-tips").andExpect {
            status { isOk() }
            jsonPath("$.items[?(@.id == '$documentId')]") { doesNotExist() }
        }
    }

    @Test
    fun expiredPublishedDailyTipIsNotListed() {
        val expiredTo = Instant.now().minus(1, ChronoUnit.DAYS)
        val documentId =
            createDailyTip(
                publish = true,
                effectiveTo = expiredTo.toString(),
            )
        mockMvc.get("/v1/content/daily-tips/$documentId").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun publishedDailyTipRespectsTargetingAndPersianBody() {
        val documentId =
            createDailyTip(
                publish = true,
                targeting =
                    mapOf(
                        "modes" to listOf("cycle"),
                        "cyclePhases" to listOf("follicular"),
                    ),
                body = "این نمونهٔ غیرپزشکی است.",
            )
        mockMvc
            .get("/v1/content/daily-tips") {
                param("mode", "cycle")
                param("cyclePhase", "follicular")
            }.andExpect {
                status { isOk() }
                jsonPath("$.items[0].body") { value("این نمونهٔ غیرپزشکی است.") }
                jsonPath("$.items[0].placeholder") { value(true) }
            }
        mockMvc
            .get("/v1/content/daily-tips") {
                param("mode", "cycle")
                param("cyclePhase", "luteal")
            }.andExpect {
                status { isOk() }
                jsonPath("$.items.length()") { value(0) }
            }
    }

    @Test
    fun programWorkflowRequiresMedicalApprovalBeforePublish() {
        val versionId = createProgramDraft()
        mockMvc
            .post("/v1/admin/content/versions/$versionId/workflow/publish") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $editorToken")
                contentType = MediaType.APPLICATION_JSON
                content = "{}"
            }.andExpect { status { isConflict() } }
    }

    @Test
    fun publishedProgramIncludesSafetyFields() {
        val documentId = createProgram(publish = true)
        mockMvc.get("/v1/content/programs/$documentId").andExpect {
            status { isOk() }
            jsonPath("$.safetyNotice") { exists() }
            jsonPath("$.sessions[0].steps[0].instruction") { value("نمونه — غیرپزشکی") }
        }
    }

    @Test
    fun collectionListsOnlyPublishedArticles() {
        val articleId = createArticle(publish = true)
        val collectionId =
            createCollection(
                publish = true,
                articleDocumentIds = listOf(articleId),
            )
        mockMvc.get("/v1/content/collections/$collectionId").andExpect {
            status { isOk() }
            jsonPath("$.articles.length()") { value(1) }
            jsonPath("$.articles[0].id") { value(articleId) }
        }
    }

    @Test
    fun illegalWorkflowTransitionIsRejected() {
        val versionId = createDailyTip(publish = false, returnVersionId = true)
        mockMvc
            .post("/v1/admin/content/versions/$versionId/workflow/publish") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $editorToken")
                contentType = MediaType.APPLICATION_JSON
                content = "{}"
            }.andExpect { status { isConflict() } }
    }

    private fun createDailyTip(
        publish: Boolean,
        effectiveTo: String? = null,
        targeting: Map<String, Any> = mapOf("modes" to listOf("cycle")),
        body: String = "نمونه",
        returnVersionId: Boolean = false,
    ): String {
        val sourceId = createSource()
        val createBody =
            mapOf(
                "slug" to "tip-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "نکتهٔ نمونه",
                "summary" to "خلاصهٔ نمونه",
                "bodyRichtext" to body,
                "contentType" to ContentTypes.DAILY_TIP,
                "medicalRiskLevel" to "none",
                "sourceIds" to listOf(sourceId),
                "typePayload" to
                    mapOf(
                        "placeholder" to true,
                        "targeting" to targeting,
                    ),
                "effectiveTo" to effectiveTo,
            ).filterValues { it != null }
        val created = createDocument(createBody)
        val versionId = created.get("versionId").asText()
        val documentId = created.get("documentId").asText()
        if (publish) {
            publishVersion(versionId)
        }
        return if (returnVersionId) versionId else documentId
    }

    private fun createProgramDraft(): String {
        val sourceId = createSource()
        val createBody =
            mapOf(
                "slug" to "program-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "برنامهٔ نمونه",
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
                                    "steps" to
                                        listOf(
                                            mapOf("instruction" to "نمونه — غیرپزشکی"),
                                        ),
                                ),
                            ),
                    ),
            )
        return createDocument(createBody).get("versionId").asText()
    }

    private fun createProgram(publish: Boolean): String {
        val versionId = createProgramDraft()
        val documentId =
            mockMvc
                .get("/v1/admin/content/versions/$versionId") {
                    header(HttpHeaders.AUTHORIZATION, "Bearer $editorToken")
                }.andReturn()
                .let { objectMapper.readTree(it.response.contentAsString).get("documentId").asText() }
        if (publish) {
            publishVersion(versionId)
        }
        return documentId
    }

    private fun createArticle(publish: Boolean): String {
        val sourceId = createSource()
        val createBody =
            mapOf(
                "slug" to "article-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "مقالهٔ نمونه",
                "bodyRichtext" to "متن نمونه",
                "contentType" to "article",
                "medicalRiskLevel" to "none",
                "sourceIds" to listOf(sourceId),
            )
        val created = createDocument(createBody)
        val versionId = created.get("versionId").asText()
        val documentId = created.get("documentId").asText()
        if (publish) {
            publishVersion(versionId)
        }
        return documentId
    }

    private fun createCollection(
        publish: Boolean,
        articleDocumentIds: List<String>,
    ): String {
        val sourceId = createSource()
        val createBody =
            mapOf(
                "slug" to "collection-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "مجموعهٔ نمونه",
                "contentType" to ContentTypes.COLLECTION,
                "medicalRiskLevel" to "none",
                "sourceIds" to listOf(sourceId),
                "typePayload" to
                    mapOf(
                        "placeholder" to true,
                        "category" to "cycle",
                        "articleDocumentIds" to articleDocumentIds,
                    ),
            )
        val created = createDocument(createBody)
        val versionId = created.get("versionId").asText()
        val documentId = created.get("documentId").asText()
        if (publish) {
            publishVersion(versionId)
        }
        return documentId
    }

    private fun createDocument(body: Map<String, Any?>): com.fasterxml.jackson.databind.JsonNode {
        val response =
            mockMvc
                .post("/v1/admin/content/documents") {
                    header(HttpHeaders.AUTHORIZATION, "Bearer $editorToken")
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(body)
                }.andExpect { status { isOk() } }
                .andReturn()
                .response.contentAsString
        return objectMapper.readTree(response)
    }

    private fun publishVersion(versionId: String) {
        workflow(versionId, "submit_medical_review")
        workflow(
            versionId,
            "approve_medical",
            mapOf("clinicalReviewer" to "dr.test@mahin.test"),
            token = medicalToken,
        )
        workflow(versionId, "approve_editorial")
        workflow(versionId, "publish")
    }

    private fun createSource(): String {
        val body =
            mapOf(
                "citationKey" to "fixture-${UUID.randomUUID()}",
                "title" to "منبع آزمون",
            )
        val response =
            mockMvc
                .post("/v1/admin/content/sources") {
                    header(HttpHeaders.AUTHORIZATION, "Bearer $editorToken")
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(body)
                }.andExpect { status { isOk() } }
                .andReturn()
                .response.contentAsString
        return objectMapper.readTree(response).get("id").asText()
    }

    private fun login(email: String): String {
        val body = mapOf("email" to email, "password" to "password-12-chars")
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
        extra: Map<String, String> = emptyMap(),
        token: String = editorToken,
    ) {
        mockMvc
            .post("/v1/admin/content/versions/$versionId/workflow/$action") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(extra)
            }.andExpect { status { isOk() } }
    }
}
