package dev.mahin.backend.content

import dev.mahin.backend.content.persistence.ContentDocumentEntity
import dev.mahin.backend.content.persistence.ContentVersionEntity
import java.time.Instant

object ContentPublicVisibility {
    fun isArticleEndpointEligible(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        now: Instant = Instant.now(),
    ): Boolean {
        if (ContentTypes.isWellnessType(version.contentType) ||
            ContentTypes.isPregnancyCompanionType(version.contentType) ||
            version.contentType == ContentTypes.PREGNANCY_WEEK
        ) {
            return false
        }
        return ContentPublicationRules.isPublishedOnCatalog(document, version, now)
    }

    fun isPlaceholderHiddenInProduction(
        placeholder: Boolean,
        environment: String,
    ): Boolean = placeholder && environment != "local" && environment != "test"
}
