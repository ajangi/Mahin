package dev.mahin.backend.content

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

object ContentTypePayloadCodec {
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

    fun serialize(
        mapper: ObjectMapper,
        node: JsonNode?,
    ): String? {
        if (node == null || node.isNull) {
            return null
        }
        return mapper.writeValueAsString(node)
    }

    private fun <T> parse(
        mapper: ObjectMapper,
        raw: String?,
        type: Class<T>,
    ): T? {
        if (raw.isNullOrBlank()) {
            return null
        }
        return runCatching { mapper.readValue(raw, type) }
            .getOrElse { throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_type_payload") }
    }
}
