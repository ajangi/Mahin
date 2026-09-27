package dev.mahin.backend.content

import dev.mahin.backend.api.currentRegisteredUser
import dev.mahin.backend.content.persistence.ContentDocumentEntity
import dev.mahin.backend.content.persistence.ContentDocumentRepository
import dev.mahin.backend.content.persistence.ContentSourceRepository
import dev.mahin.backend.content.persistence.ContentVersionRepository
import dev.mahin.backend.content.persistence.ContentVersionSourceRepository
import dev.mahin.backend.content.persistence.UserContentBookmarkEntity
import dev.mahin.backend.content.persistence.UserContentBookmarkRepository
import java.time.Instant
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class ContentPublicService(
    private val documentRepository: ContentDocumentRepository,
    private val versionRepository: ContentVersionRepository,
    private val versionSourceRepository: ContentVersionSourceRepository,
    private val sourceRepository: ContentSourceRepository,
    private val bookmarkRepository: UserContentBookmarkRepository,
    private val catalogService: ContentCatalogService,
) {
    fun catalogStatus(): ContentCatalogStatusResponse = catalogService.currentStatus()

    fun getArticle(articleId: UUID): ContentArticleResponse {
        val document =
            documentRepository.findById(articleId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "article_not_found")
            }
        return mapPublishedArticle(document)
    }

    private fun mapPublishedArticle(document: ContentDocumentEntity): ContentArticleResponse {
        val publishedVersionId = document.publishedVersionId
        if (publishedVersionId == null || document.withdrawnAt != null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "article_not_found")
        }
        val version =
            versionRepository.findById(publishedVersionId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "article_not_found")
            }
        if (version.status != "published") {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "article_not_found")
        }
        return mapArticle(document, version, includeBody = true)
    }

    fun getArticleBySlug(
        slug: String,
        locale: String,
    ): ContentArticleResponse {
        val document =
            documentRepository.findBySlugAndLocale(slug, locale).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "article_not_found")
            }
        return getArticle(document.id)
    }

    fun search(
        query: String?,
        locale: String?,
        lifeStage: String?,
    ): ContentSearchResponse {
        val normalized = query?.let { PersianSearchNormalizer.normalize(it) }
        val items =
            versionRepository.searchPublished(normalized, locale, lifeStage).map { version ->
                val document =
                    documentRepository.findById(version.documentId).orElseThrow()
                ContentArticleSummary(
                    id = document.id.toString(),
                    slug = document.slug,
                    locale = document.locale,
                    title = version.title,
                    summary = version.summary,
                    lifeStage = version.lifeStage,
                    contentType = version.contentType,
                    gestationalWeek = version.gestationalWeek,
                )
            }
        return ContentSearchResponse(items)
    }

    fun pregnancyWeek(
        week: Int,
        locale: String,
    ): ContentArticleResponse {
        if (week !in 1..42) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_gestational_week")
        }
        val version =
            versionRepository.findPublishedPregnancyWeek(week, locale).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "pregnancy_week_not_found")
            }
        val document =
            documentRepository.findById(version.documentId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "pregnancy_week_not_found")
            }
        return mapArticle(document, version, includeBody = true)
    }

    fun listPregnancyWeeks(locale: String): PregnancyWeekListResponse {
        val weeks =
            versionRepository
                .searchPublished(
                    null,
                    locale,
                    "pregnancy",
                ).mapNotNull { it.gestationalWeek }
                .distinct()
                .sorted()
        return PregnancyWeekListResponse(locale = locale, weeks = weeks)
    }

    @Transactional
    fun addBookmark(documentId: UUID) {
        val user = currentRegisteredUser()
        if (!documentRepository.existsById(documentId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "article_not_found")
        }
        if (!bookmarkRepository.existsByUserIdAndDocumentId(user.userId, documentId)) {
            bookmarkRepository.save(
                UserContentBookmarkEntity(
                    userId = user.userId,
                    documentId = documentId,
                    createdAt = Instant.now(),
                ),
            )
        }
    }

    @Transactional
    fun removeBookmark(documentId: UUID) {
        val user = currentRegisteredUser()
        bookmarkRepository.deleteById(UserContentBookmarkEntity.Pk(user.userId, documentId))
    }

    fun listBookmarks(): ContentBookmarkListResponse {
        val user = currentRegisteredUser()
        val bookmarks = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(user.userId)
        val summaries =
            bookmarks.mapNotNull { bookmark ->
                val document = documentRepository.findById(bookmark.documentId).orElse(null) ?: return@mapNotNull null
                val versionId = document.publishedVersionId ?: return@mapNotNull null
                val version = versionRepository.findById(versionId).orElse(null) ?: return@mapNotNull null
                ContentArticleSummary(
                    id = document.id.toString(),
                    slug = document.slug,
                    locale = document.locale,
                    title = version.title,
                    summary = version.summary,
                    lifeStage = version.lifeStage,
                    contentType = version.contentType,
                    gestationalWeek = version.gestationalWeek,
                )
            }
        return ContentBookmarkListResponse(summaries)
    }

    private fun mapArticle(
        document: ContentDocumentEntity,
        version: dev.mahin.backend.content.persistence.ContentVersionEntity,
        includeBody: Boolean,
    ): ContentArticleResponse {
        val citationKeys =
            versionSourceRepository.findByVersionId(version.id).mapNotNull { link ->
                sourceRepository.findById(link.sourceId).orElse(null)?.citationKey
            }
        return ContentMapper.toArticleResponse(document, version, citationKeys, includeBody)
    }
}
