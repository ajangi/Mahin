package dev.mahin.backend.assistant

import dev.mahin.backend.content.PersianSearchNormalizer
import dev.mahin.backend.content.persistence.ContentDocumentRepository
import dev.mahin.backend.content.persistence.ContentVersionRepository
import java.util.UUID
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

/**
 * Fixed non-medical fixture corpus for CI evaluation when CMS is empty.
 */
object AssistantFixtureCorpus {
    val FIXTURE_DOCUMENT_ID: UUID = UUID.fromString("00000000-0000-4000-8000-00000000a12a")
    val FIXTURE_VERSION_ID: UUID = UUID.fromString("00000000-0000-4000-8000-00000000a12b")

    val chunks: List<RetrievedContentChunk> =
        listOf(
            RetrievedContentChunk(
                documentId = FIXTURE_DOCUMENT_ID,
                versionId = FIXTURE_VERSION_ID,
                slug = "fixture-m12-non-medical",
                title = "نمونهٔ آموزشی (غیرپزشکی) — M12",
                excerpt =
                    "این متن صرفاً برای آزمایش زیرساخت دستیار است و توصیهٔ پزشکی نیست. " +
                        "برای سوالات فوری با پزشک تماس بگیرید.",
                locale = "fa-IR",
            ),
        )

    fun match(question: String): List<RetrievedContentChunk> {
        val normalized = PersianSearchNormalizer.normalize(question)
        return if (normalized.contains("fixture") || normalized.contains("نمونه")) {
            chunks
        } else {
            emptyList()
        }
    }
}

@Component
class CompositeApprovedContentRetriever(
    private val cmsRetriever: CmsKeywordApprovedContentRetriever,
) : ApprovedContentRetriever {
    override fun retrieve(
        question: String,
        locale: String,
        limit: Int,
    ): List<RetrievedContentChunk> {
        val cms = cmsRetriever.retrieve(question, locale, limit)
        if (cms.isNotEmpty()) return cms
        return AssistantFixtureCorpus.match(question).take(limit)
    }
}
