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
        validatePayloadSize(typePayloadJson)
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
        validatePayloadSize(typePayloadJson)
    }

    fun validatePayloadSize(typePayloadJson: String?) {
        if (typePayloadJson != null &&
            typePayloadJson.toByteArray(Charsets.UTF_8).size > ContentTypePayloadLimits.MAX_PAYLOAD_BYTES
        ) {
            reject("type_payload_too_large")
        }
    }

    fun validateArticleDocumentIds(ids: List<String>) {
        if (ids.size > ContentTypePayloadLimits.MAX_COLLECTION_ARTICLES) {
            reject("collection_article_ids_too_many")
        }
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
        if (publishing &&
            (payload.safetyNotice.isNullOrBlank() || payload.contraindications.isNullOrBlank())
        ) {
            reject("program_safety_fields_required")
        }
        when {
            payload.programType !in programKinds -> reject("invalid_program_type")
            payload.sessions.size > ContentTypePayloadLimits.MAX_SESSIONS -> reject("program_sessions_too_many")
            payload.sessions.isEmpty() -> reject("program_sessions_required")
            payload.sessions.any { it.steps.isEmpty() } -> reject("program_steps_required")
            payload.sessions.any { it.steps.size > ContentTypePayloadLimits.MAX_STEPS_PER_SESSION } ->
                reject("program_steps_too_many")
            payload.sessions.any { session -> session.steps.any { it.instruction.isBlank() } } ->
                reject("program_step_instruction_required")
            else -> validateProgramStrings(payload)
        }
        payload.sessions.forEach { session ->
            session.steps.forEach { step -> validateStepNumbers(step) }
        }
    }

    private fun validateProgramStrings(payload: ProgramPayload) {
        listOfNotNull(
            payload.safetyNotice,
            payload.contraindications,
            payload.essentialSafetyInformation,
        ).forEach { validateStringLength(it) }
        payload.sessions.forEach { session ->
            validateStringLength(session.title)
            session.steps.forEach { validateStringLength(it.instruction) }
        }
    }

    private fun validateStepNumbers(step: ProgramStepPayload) {
        step.durationSeconds?.let { requireNonNegative(it, "invalid_program_step_duration") }
        step.reps?.let { requireNonNegative(it, "invalid_program_step_reps") }
        step.restSeconds?.let { requireNonNegative(it, "invalid_program_step_rest") }
    }

    private fun validateCollection(payload: CollectionPayload) {
        if (payload.category !in collectionCategories) {
            reject("invalid_collection_category")
        }
        validateArticleDocumentIds(payload.articleDocumentIds)
    }

    private fun validateStringLength(value: String) {
        if (value.length > ContentTypePayloadLimits.MAX_STRING_CHARS) {
            reject("type_payload_string_too_long")
        }
    }

    private fun requireNonNegative(
        value: Int,
        code: String,
    ) {
        if (value < 0) {
            reject(code)
        }
    }

    private fun reject(code: String): Unit = throw ResponseStatusException(HttpStatus.BAD_REQUEST, code)
}
