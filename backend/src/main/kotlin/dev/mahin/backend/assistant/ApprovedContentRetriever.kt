package dev.mahin.backend.assistant

import dev.mahin.backend.content.PersianSearchNormalizer
import dev.mahin.backend.content.persistence.ContentDocumentRepository
import dev.mahin.backend.content.persistence.ContentVersionRepository
import org.springframework.stereotype.Component

interface ApprovedContentRetriever {
    fun retrieve(
        question: String,
        locale: String,
        limit: Int = 3,
    ): List<RetrievedContentChunk>
}

/**
 * Keyword retrieval over published CMS content only (no embeddings in M12).
 */
@Component
class CmsKeywordApprovedContentRetriever(
    private val versionRepository: ContentVersionRepository,
    private val documentRepository: ContentDocumentRepository,
) : ApprovedContentRetriever {
    override fun retrieve(
        question: String,
        locale: String,
        limit: Int,
    ): List<RetrievedContentChunk> {
        val normalized = PersianSearchNormalizer.normalize(question)
        val versions =
            versionRepository.searchPublished(
                query = normalized.ifBlank { null },
                locale = locale,
                lifeStage = null,
            )
        return versions.take(limit).mapNotNull { version ->
            val document = documentRepository.findById(version.documentId).orElse(null) ?: return@mapNotNull null
            RetrievedContentChunk(
                documentId = document.id,
                versionId = version.id,
                slug = document.slug,
                title = version.title,
                excerpt = version.summary ?: version.title,
                locale = document.locale,
            )
        }
    }
}
