package dev.mahin.backend.content

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.web.server.ResponseStatusException

class ContentTypePayloadValidatorTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun rejectsMalformedJsonWithInvalidTypePayload() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                ContentTypePayloadCodec.parseProgram(mapper, "{not-json")
            }
        assertEquals("invalid_type_payload", ex.reason)
    }

    @Test
    fun rejectsUnknownFieldsWithShapeCode() {
        val node =
            mapper.readTree(
                """
                {"placeholder":true,"programType":"breathing","unknown":1,"sessions":[{"id":"s","title":"t","order":1,"steps":[{"instruction":"x"}]}],"safetyNotice":"a","contraindications":"b"}
                """.trimIndent(),
            )
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                ContentTypePayloadCodec.encodeValidated(mapper, ContentTypes.PROGRAM, node)
            }
        assertEquals("invalid_type_payload", ex.reason)
    }

    @Test
    fun rejectsOversizedPayload() {
        val padding = "x".repeat(ContentTypePayloadLimits.MAX_PAYLOAD_BYTES)
        val json =
            """
            {"placeholder":true,"programType":"breathing","safetyNotice":"a","contraindications":"b","sessions":[{"id":"s1","title":"t","order":1,"steps":[{"instruction":"$padding"}]}]}
            """.trimIndent()
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                ContentTypePayloadValidator.validatePayloadSize(json)
            }
        assertEquals("type_payload_too_large", ex.reason)
    }

    @Test
    fun rejectsInvalidCareScheduleWeekWindow() {
        val json =
            """
            {"placeholder":true,"regionContext":"iran","items":[{"id":"i1","title":"t","category":"test","weekStart":12,"weekEnd":8}]}
            """.trimIndent()
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                ContentTypePayloadValidator.validateForPublish(ContentTypes.CARE_SCHEDULE, json, mapper)
            }
        assertEquals("invalid_care_schedule_week_window", ex.reason)
    }

    @Test
    fun requiresSafetyFieldsOnPublishEvenForPlaceholder() {
        val json =
            """
            {"placeholder":true,"programType":"breathing","sessions":[{"id":"s1","title":"جلسه","order":1,"steps":[{"instruction":"نمونه"}]}]}
            """.trimIndent()
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                ContentTypePayloadValidator.validateForPublish(ContentTypes.PROGRAM, json, mapper)
            }
        assertEquals("program_safety_fields_required", ex.reason, "expected safety validation on publish")
    }

    @Test
    fun scriptTagRoundTripsAsLiteralText() {
        val script = "<script>alert(1)</script>"
        val json =
            mapper.writeValueAsString(
                ProgramPayload(
                    placeholder = true,
                    programType = "movement",
                    safetyNotice = "safe",
                    contraindications = "none",
                    sessions =
                        listOf(
                            ProgramSessionPayload(
                                id = "s1",
                                title = "t",
                                order = 1,
                                steps = listOf(ProgramStepPayload(instruction = script)),
                            ),
                        ),
                ),
            )
        val parsed = ContentTypePayloadCodec.parseProgram(mapper, json)!!
        assertEquals(
            script,
            parsed.sessions
                .first()
                .steps
                .first()
                .instruction,
        )
    }

    @Test
    fun rejectsProgramWithNoneRiskLevel() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                ContentWorkflowPolicy.validateMedicalRiskForContentType(
                    ContentTypes.PROGRAM,
                    MedicalRiskLevel.NONE,
                )
            }
        assertEquals("wellness_requires_clinical_review", ex.reason)
    }

    @Test
    fun rejectsCareScheduleWithInvalidWeekWindow() {
        val json =
            """
            {"placeholder":true,"regionContext":"iran","items":[{"id":"i","title":"t","category":"test","weekStart":12,"weekEnd":8}]}
            """.trimIndent()
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                ContentTypePayloadValidator.validateForSave(ContentTypes.CARE_SCHEDULE, json, mapper)
            }
        assertEquals("invalid_care_schedule_week_window", ex.reason)
    }

    @Test
    fun rejectsWarningSignsWithoutSignItems() {
        val json = warningSignsJson(signs = "[]")
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                ContentTypePayloadValidator.validateForSave(ContentTypes.WARNING_SIGNS, json, mapper)
            }
        assertEquals("warning_signs_required", ex.reason)
    }

    @Test
    fun rejectsWarningSignWithBlankFields() {
        val json =
            warningSignsJson(
                signs =
                    """[{"id":"","title":"t","summary":"s"}]""",
            )
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                ContentTypePayloadValidator.validateForSave(ContentTypes.WARNING_SIGNS, json, mapper)
            }
        assertEquals("warning_sign_fields_required", ex.reason)
    }

    @Test
    fun rejectsInvalidBpThreshold() {
        val json =
            warningSignsJson(
                bpThresholds =
                    """{"elevatedSystolic":300,"elevatedDiastolic":90,"urgentSystolic":160,"urgentDiastolic":110}""",
            )
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                ContentTypePayloadValidator.validateForSave(ContentTypes.WARNING_SIGNS, json, mapper)
            }
        assertEquals("invalid_bp_threshold", ex.reason)
    }

    @Test
    fun rejectsInvalidBpThresholdOrder() {
        val json =
            warningSignsJson(
                bpThresholds =
                    """{"elevatedSystolic":170,"elevatedDiastolic":90,"urgentSystolic":160,"urgentDiastolic":110}""",
            )
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                ContentTypePayloadValidator.validateForSave(ContentTypes.WARNING_SIGNS, json, mapper)
            }
        assertEquals("invalid_bp_threshold_order", ex.reason)
    }

    private fun warningSignsJson(
        signs: String =
            """[{"id":"s1","title":"t","summary":"s"}]""",
        bpThresholds: String =
            """{"elevatedSystolic":140,"elevatedDiastolic":90,"urgentSystolic":160,"urgentDiastolic":110}""",
    ): String =
        """
        {
          "placeholder": true,
          "introTitle": "i",
          "introBody": "b",
          "signs": $signs,
          "escalationTitle": "e",
          "escalationBody": "e",
          "contactClinicianLabel": "c",
          "urgentEscalationTitle": "u",
          "urgentEscalationBody": "u",
          "urgentContactClinicianLabel": "u",
          "bpThresholds": $bpThresholds
        }
        """.trimIndent()
}
