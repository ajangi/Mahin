package dev.mahin.backend.content

import dev.mahin.backend.content.persistence.ContentDocumentEntity
import dev.mahin.backend.content.persistence.ContentVersionEntity
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ContentPublicationRulesTest {
    @Test
    fun expiredContentIsNotWithinEffectiveWindow() {
        val version =
            ContentVersionEntity(
                effectiveFrom = Instant.parse("2020-01-01T00:00:00Z"),
                effectiveTo = Instant.parse("2020-01-02T00:00:00Z"),
            )
        assertFalse(
            ContentPublicationRules.isWithinEffectiveWindow(
                version,
                Instant.parse("2021-01-01T00:00:00Z"),
            ),
        )
    }

    @Test
    fun exactlyAtEffectiveToIsExcluded() {
        val boundary = Instant.parse("2026-06-01T12:00:00Z")
        val version = ContentVersionEntity(effectiveTo = boundary)
        assertFalse(ContentPublicationRules.isWithinEffectiveWindow(version, boundary))
    }

    @Test
    fun instantBeforeEffectiveToIsIncluded() {
        val boundary = Instant.parse("2026-06-01T12:00:00Z")
        val version = ContentVersionEntity(effectiveTo = boundary)
        assertTrue(ContentPublicationRules.isWithinEffectiveWindow(version, boundary.minusMillis(1)))
    }

    @Test
    fun futureEffectiveFromIsExcluded() {
        val version = ContentVersionEntity(effectiveFrom = Instant.parse("2099-01-01T00:00:00Z"))
        assertFalse(ContentPublicationRules.isWithinEffectiveWindow(version, Instant.parse("2026-01-01T00:00:00Z")))
    }

    @Test
    fun dailyTipTargetingRequiresModeWhenConfigured() {
        val payload =
            DailyTipPayload(
                targeting = DailyTipTargeting(modes = listOf("pregnancy")),
            )
        assertFalse(
            ContentPublicationRules.matchesDailyTipTargeting(
                payload,
                mode = "cycle",
                cyclePhase = null,
                pregnancyWeek = null,
            ),
        )
        assertTrue(
            ContentPublicationRules.matchesDailyTipTargeting(
                payload,
                mode = "pregnancy",
                cyclePhase = null,
                pregnancyWeek = 12,
            ),
        )
    }

    @Test
    fun publishedCatalogRequiresMatchingPublishedVersion() {
        val documentId = UUID.randomUUID()
        val versionId = UUID.randomUUID()
        val document =
            ContentDocumentEntity(
                id = documentId,
                slug = "tip",
                locale = "fa-IR",
                publishedVersionId = versionId,
            )
        val version =
            ContentVersionEntity(
                id = versionId,
                documentId = documentId,
                title = "نمونه",
                status = "published",
            )
        assertTrue(ContentPublicationRules.isPublishedOnCatalog(document, version))
        val draft =
            ContentVersionEntity(
                id = UUID.randomUUID(),
                documentId = documentId,
                title = "نمونه",
                status = "draft",
            )
        assertFalse(ContentPublicationRules.isPublishedOnCatalog(document, draft))
    }
}
