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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class M17aWellnessContentIntegrationTest(
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
    fun publishedDailyTipRespectsTargetingAndPersianBody() {
        val persianBody = "این نمونهٔ غیرپزشکی است."
        val documentId =
            createDailyTip(
                publish = true,
                body = persianBody,
                targeting =
                    mapOf(
                        "modes" to listOf("cycle", "pregnancy"),
                        "cyclePhases" to listOf("follicular"),
                        "pregnancyWeeks" to listOf(12),
                    ),
            )
        mockMvc
            .get("/v1/content/daily-tips") {
                param("mode", "cycle")
                param("cyclePhase", "follicular")
                param("pregnancyWeek", "12")
            }.andExpect {
                status { isOk() }
                jsonPath("$.items[?(@.id == '$documentId')].id") { value(documentId) }
            }
        mockMvc
            .get("/v1/content/daily-tips") {
                param("mode", "cycle")
                param("cyclePhase", "luteal")
                param("pregnancyWeek", "12")
            }.andExpect {
                status { isOk() }
                jsonPath("$.items[?(@.id == '$documentId')]") { doesNotExist() }
            }
        mockMvc
            .get("/v1/content/daily-tips") {
                param("mode", "pregnancy")
                param("pregnancyWeek", "12")
                param("cyclePhase", "follicular")
            }.andExpect {
                status { isOk() }
                jsonPath("$.items[?(@.id == '$documentId')].id") { value(documentId) }
            }
        mockMvc
            .get("/v1/content/daily-tips") {
                param("mode", "pregnancy")
                param("pregnancyWeek", "13")
                param("cyclePhase", "follicular")
            }.andExpect {
                status { isOk() }
                jsonPath("$.items[?(@.id == '$documentId')]") { doesNotExist() }
            }
        mockMvc.get("/v1/content/daily-tips/$documentId").andExpect {
            status { isOk() }
            jsonPath("$.body") { value(persianBody) }
        }
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

    private fun createDailyTip(
        publish: Boolean,
        body: String,
        targeting: Map<String, Any>,
    ): String {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val bodyMap =
            mapOf(
                "slug" to "tip-${UUID.randomUUID()}",
                "locale" to "fa-IR",
                "title" to "نکتهٔ نمونه",
                "summary" to "خلاصه",
                "bodyRichtext" to body,
                "contentType" to ContentTypes.DAILY_TIP,
                "medicalRiskLevel" to "general_education",
                "sourceIds" to listOf(sourceId),
                "typePayload" to
                    mapOf(
                        "placeholder" to true,
                        "targeting" to targeting,
                    ),
            )
        val created = createDocument(bodyMap)
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

    private fun createProgram(publish: Boolean): String {
        val sourceId = WellnessContentTestSupport.createSource(mockMvc, objectMapper, tokens.editorToken)
        val bodyMap =
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
                        "safetyNotice" to "نمونه — غیرپزشکی",
                        "contraindications" to "نمونه — غیرپزشکی",
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
        val created = createDocument(bodyMap)
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
}
