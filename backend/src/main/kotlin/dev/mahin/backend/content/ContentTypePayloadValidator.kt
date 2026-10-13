package dev.mahin.backend.content

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

object ContentTypePayloadValidator {
    private val programKinds = setOf("pelvic_floor", "movement", "breathing")
    private val collectionCategories =
        setOf(
            "cycle",
            "fertility",
            "pregnancy",
            "symptoms",
            "nutrition",
            "tests",
            "lifestyle",
        )
    private val targetingModes = setOf("cycle", "ttc", "pregnancy")
    private val cyclePhases = setOf("menstrual", "follicular", "ovulation", "luteal")

    fun validateForSave(
        contentType: String,
        typePayloadJson: String?,
        mapper: ObjectMapper,
    ) {
        when {
            !ContentTypes.isWellnessType(contentType) && !typePayloadJson.isNullOrBlank() ->
                reject("type_payload_not_allowed")
            ContentTypes.isWellnessType(contentType) && typePayloadJson.isNullOrBlank() ->
                reject("type_payload_required")
            contentType == ContentTypes.DAILY_TIP ->
                validateDailyTip(ContentTypePayloadCodec.parseDailyTip(mapper, typePayloadJson)!!)
            contentType == ContentTypes.PROGRAM ->
                validateProgram(ContentTypePayloadCodec.parseProgram(mapper, typePayloadJson)!!, publishing = false)
            contentType == ContentTypes.COLLECTION ->
                validateCollection(ContentTypePayloadCodec.parseCollection(mapper, typePayloadJson)!!)
        }
    }

    fun validateForPublish(
        contentType: String,
        typePayloadJson: String?,
        mapper: ObjectMapper,
    ) {
        if (!ContentTypes.isWellnessType(contentType)) {
            return
        }
        when (contentType) {
            ContentTypes.DAILY_TIP ->
                validateDailyTip(ContentTypePayloadCodec.parseDailyTip(mapper, typePayloadJson)!!)
            ContentTypes.PROGRAM ->
                validateProgram(ContentTypePayloadCodec.parseProgram(mapper, typePayloadJson)!!, publishing = true)
            ContentTypes.COLLECTION ->
                validateCollection(ContentTypePayloadCodec.parseCollection(mapper, typePayloadJson)!!)
            else -> reject("unsupported_content_type")
        }
    }

    fun validateArticleDocumentIds(ids: List<String>) {
        val invalid = ids.any { runCatching { UUID.fromString(it) }.isFailure }
        if (invalid) {
            reject("invalid_article_document_id")
        }
    }

    private fun validateDailyTip(payload: DailyTipPayload) {
        val invalidMode = payload.targeting.modes.any { it !in targetingModes }
        val invalidPhase = payload.targeting.cyclePhases.any { it !in cyclePhases }
        val invalidWeek = payload.targeting.pregnancyWeeks.any { it !in 1..42 }
        if (invalidMode || invalidPhase || invalidWeek) {
            reject(
                when {
                    invalidMode -> "invalid_daily_tip_mode"
                    invalidPhase -> "invalid_daily_tip_cycle_phase"
                    else -> "invalid_daily_tip_pregnancy_week"
                },
            )
        }
    }

    private fun validateProgram(
        payload: ProgramPayload,
        publishing: Boolean,
    ) {
        when {
            payload.programType !in programKinds -> reject("invalid_program_type")
            payload.sessions.isEmpty() -> reject("program_sessions_required")
            payload.sessions.any { it.steps.isEmpty() } -> reject("program_steps_required")
            payload.sessions.any { session -> session.steps.any { it.instruction.isBlank() } } ->
                reject("program_step_instruction_required")
            publishing &&
                !payload.placeholder &&
                (payload.safetyNotice.isNullOrBlank() || payload.contraindications.isNullOrBlank()) ->
                reject("program_safety_fields_required")
        }
    }

    private fun validateCollection(payload: CollectionPayload) {
        if (payload.category !in collectionCategories) {
            reject("invalid_collection_category")
        }
        validateArticleDocumentIds(payload.articleDocumentIds)
    }

    private fun reject(code: String): Unit = throw ResponseStatusException(HttpStatus.BAD_REQUEST, code)
}
