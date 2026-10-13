package dev.mahin.backend.content

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.web.server.ResponseStatusException

class ContentEffectiveDatesTest {
    @Test
    fun rejectsInvalidInstant() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                ContentEffectiveDates.parseInstant("not-a-date")
            }
        assertEquals("invalid_effective_window", ex.reason)
    }

    @Test
    fun rejectsEffectiveToBeforeFrom() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                ContentEffectiveDates.validateForContentType(
                    ContentTypes.DAILY_TIP,
                    java.time.Instant.parse("2026-01-02T00:00:00Z"),
                    java.time.Instant.parse("2026-01-01T00:00:00Z"),
                )
            }
        assertEquals("invalid_effective_window", ex.reason)
    }

    @Test
    fun rejectsInvalidNextReviewDueAt() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                ContentEffectiveDates.parseNextReviewDueAt("not-an-instant")
            }
        assertEquals("invalid_next_review_due_at", ex.reason)
    }

    @Test
    fun rejectsEffectiveWindowOnArticle() {
        val ex =
            assertThrows(ResponseStatusException::class.java) {
                ContentEffectiveDates.validateForContentType(
                    ContentTypes.ARTICLE,
                    java.time.Instant.now(),
                    null,
                )
            }
        assertEquals("effective_window_not_allowed", ex.reason)
    }
}
