package dev.mahin.backend.assistant

import dev.mahin.backend.content.PersianSearchNormalizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

/**
 * Non-medical fixture corpus for local/test/dev CI only — never active in production profiles.
 */
@Configuration
@Profile("local", "test", "dev")
class AssistantDevRetrievalConfiguration {
    @Bean
    @Primary
    fun devApprovedContentRetriever(cmsRetriever: CmsKeywordApprovedContentRetriever): ApprovedContentRetriever =
        FixtureFallbackApprovedContentRetriever(cmsRetriever)
}

internal class FixtureFallbackApprovedContentRetriever(
    private val cmsRetriever: CmsKeywordApprovedContentRetriever,
) : ApprovedContentRetriever {
    override fun retrieve(
        question: String,
        locale: String,
        limit: Int,
    ): List<RetrievedContentChunk> {
        val cms = cmsRetriever.retrieve(question, locale, limit)
        if (cms.isNotEmpty()) {
            return cms
        }
        return AssistantFixtureCorpus.match(question).take(limit)
    }
}

internal object AssistantFixtureCorpus {
    val chunks: List<RetrievedContentChunk> =
        listOf(
            RetrievedContentChunk(
                documentId = java.util.UUID.fromString("00000000-0000-4000-8000-00000000a12a"),
                versionId = java.util.UUID.fromString("00000000-0000-4000-8000-00000000a12b"),
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
        return if (normalized.contains("fixture") || normalized.contains("m12-fixture-token")) {
            chunks
        } else {
            emptyList()
        }
    }
}
