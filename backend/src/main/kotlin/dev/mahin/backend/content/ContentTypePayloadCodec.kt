package dev.mahin.backend.content

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

object ContentTypePayloadCodec {
    fun encodeValidated(
        mapper: ObjectMapper,
        contentType: String,
        node: JsonNode?,
    ): String? {
        if (node == null || node.isNull) {
            return null
        }
        val rawBytes =
            runCatching { mapper.writeValueAsBytes(node) }
                .getOrElse { badRequest("invalid_type_payload") }
        if (rawBytes.size > ContentTypePayloadLimits.MAX_PAYLOAD_BYTES) {
            badRequest("type_payload_too_large")
        }
        val strict = strictMapper(mapper)
        val canonical =
            when (contentType) {
                ContentTypes.DAILY_TIP ->
                    strict.writeValueAsString(parseDailyTip(strict, nodeToJson(strict, node)))
                ContentTypes.PROGRAM ->
                    strict.writeValueAsString(parseProgram(strict, nodeToJson(strict, node)))
                ContentTypes.COLLECTION ->
                    strict.writeValueAsString(parseCollection(strict, nodeToJson(strict, node)))
                ContentTypes.PREGNANCY_WEEK ->
                    strict.writeValueAsString(parsePregnancyWeek(strict, nodeToJson(strict, node)))
                ContentTypes.CARE_SCHEDULE ->
                    strict.writeValueAsString(parseCareSchedule(strict, nodeToJson(strict, node)))
                ContentTypes.CHECKLIST_TEMPLATE ->
                    strict.writeValueAsString(parseChecklistTemplate(strict, nodeToJson(strict, node)))
                ContentTypes.WARNING_SIGNS ->
                    strict.writeValueAsString(parseWarningSigns(strict, nodeToJson(strict, node)))
                else -> badRequest("type_payload_not_allowed")
            }
        ContentTypePayloadValidator.validatePayloadSize(canonical)
        return canonical
    }

    fun parseDailyTip(
        mapper: ObjectMapper,
        raw: String?,
    ): DailyTipPayload? = parse(mapper, raw, DailyTipPayload::class.java)

    fun parseProgram(
        mapper: ObjectMapper,
        raw: String?,
    ): ProgramPayload? = parse(mapper, raw, ProgramPayload::class.java)

    fun parseCollection(
        mapper: ObjectMapper,
        raw: String?,
    ): CollectionPayload? = parse(mapper, raw, CollectionPayload::class.java)

    fun parsePregnancyWeek(
        mapper: ObjectMapper,
        raw: String?,
    ): PregnancyWeekPayload? = parse(mapper, raw, PregnancyWeekPayload::class.java)

    fun parseCareSchedule(
        mapper: ObjectMapper,
        raw: String?,
    ): CareSchedulePayload? = parse(mapper, raw, CareSchedulePayload::class.java)

    fun parseChecklistTemplate(
        mapper: ObjectMapper,
        raw: String?,
    ): ChecklistTemplatePayload? = parse(mapper, raw, ChecklistTemplatePayload::class.java)

    fun parseWarningSigns(
        mapper: ObjectMapper,
        raw: String?,
    ): WarningSignsPayload? = parse(mapper, raw, WarningSignsPayload::class.java)

    fun placeholderFlag(
        mapper: ObjectMapper,
        contentType: String,
        typePayloadJson: String?,
    ): Boolean =
        when (contentType) {
            ContentTypes.DAILY_TIP -> parseDailyTip(mapper, typePayloadJson)?.placeholder
            ContentTypes.PROGRAM -> parseProgram(mapper, typePayloadJson)?.placeholder
            ContentTypes.COLLECTION -> parseCollection(mapper, typePayloadJson)?.placeholder
            ContentTypes.CARE_SCHEDULE -> parseCareSchedule(mapper, typePayloadJson)?.placeholder
            ContentTypes.CHECKLIST_TEMPLATE -> parseChecklistTemplate(mapper, typePayloadJson)?.placeholder
            ContentTypes.WARNING_SIGNS -> parseWarningSigns(mapper, typePayloadJson)?.placeholder
            ContentTypes.PREGNANCY_WEEK -> parsePregnancyWeek(mapper, typePayloadJson)?.placeholder
            else -> false
        } ?: false

    private fun nodeToJson(
        mapper: ObjectMapper,
        node: JsonNode,
    ): String = mapper.writeValueAsString(node)

    private fun strictMapper(mapper: ObjectMapper): ObjectMapper =
        mapper.copy().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)

    private fun <T> parse(
        mapper: ObjectMapper,
        raw: String?,
        type: Class<T>,
    ): T? {
        if (raw.isNullOrBlank()) {
            return null
        }
        return runCatching { mapper.readValue(raw, type) }
            .getOrElse { ex ->
                val code =
                    if (ex is com.fasterxml.jackson.core.JsonProcessingException) {
                        "invalid_type_payload"
                    } else {
                        "invalid_type_payload_shape"
                    }
                badRequest(code)
            }
    }

    private fun badRequest(code: String): Nothing = throw ResponseStatusException(HttpStatus.BAD_REQUEST, code)
}
