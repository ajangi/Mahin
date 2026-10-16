package dev.mahin.backend.content

import java.time.Instant
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

object ContentEffectiveDates {
    fun parseInstant(raw: String?): Instant? {
        if (raw.isNullOrBlank()) {
            return null
        }
        return runCatching { Instant.parse(raw) }
            .getOrElse { throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_effective_window") }
    }

    fun parseNextReviewDueAt(raw: String?): Instant? {
        if (raw.isNullOrBlank()) {
            return null
        }
        return runCatching { Instant.parse(raw) }
            .getOrElse { throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_next_review_due_at") }
    }

    fun validateForContentType(
        contentType: String,
        effectiveFrom: Instant?,
        effectiveTo: Instant?,
    ) {
        val hasWindow = effectiveFrom != null || effectiveTo != null
        if (hasWindow &&
            !ContentTypes.isWellnessType(contentType) &&
            !ContentTypes.isPregnancyCompanionType(contentType)
        ) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "effective_window_not_allowed")
        }
        if (effectiveFrom != null && effectiveTo != null && !effectiveTo.isAfter(effectiveFrom)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_effective_window")
        }
    }
}
