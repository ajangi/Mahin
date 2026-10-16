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
@Suppress("LongParameterList")
class ContentPublicService(
    private val documentRepository: ContentDocumentRepository,
    private val versionRepository: ContentVersionRepository,
    private val versionSourceRepository: ContentVersionSourceRepository,
    private val sourceRepository: ContentSourceRepository,
    private val bookmarkRepository: UserContentBookmarkRepository,
    private val catalogService: ContentCatalogService,
    private val objectMapper: com.fasterxml.jackson.databind.ObjectMapper,
    @org.springframework.beans.factory.annotation.Value("\${mahin.environment}") private val environment: String,
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
        if (!ContentPublicVisibility.isArticleEndpointEligible(document, version)) {
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
        val now = Instant.now()
        val items =
            versionRepository.searchPublished(normalized, locale, lifeStage).mapNotNull { version ->
                val document =
                    documentRepository.findById(version.documentId).orElse(null) ?: return@mapNotNull null
                if (!ContentPublicVisibility.isArticleEndpointEligible(document, version, now)) {
                    return@mapNotNull null
                }
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
        val loaded = loadPublishedPregnancyWeek(week, locale)
        return mapArticle(loaded.document, loaded.version, includeBody = true)
    }

    private fun loadPublishedPregnancyWeek(
        week: Int,
        locale: String,
    ): PregnancyWeekPublication {
        val version =
            versionRepository.findPublishedPregnancyWeek(week, locale).orElse(null)
        val document =
            version?.let { documentRepository.findById(it.documentId).orElse(null) }
        val visible =
            version != null &&
                document != null &&
                ContentPublicationRules.isPublishedOnCatalog(document, version) &&
                !isHiddenPregnancyWeekPlaceholder(version)
        if (!visible || version == null || document == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "pregnancy_week_not_found")
        }
        return PregnancyWeekPublication(document, version)
    }

    private fun isHiddenPregnancyWeekPlaceholder(
        version: dev.mahin.backend.content.persistence.ContentVersionEntity,
    ): Boolean {
        val payload = ContentTypePayloadCodec.parsePregnancyWeek(objectMapper, version.typePayloadJson)
        return payload != null &&
            ContentPublicVisibility.isPlaceholderHiddenInProduction(payload.placeholder, environment)
    }

    private data class PregnancyWeekPublication(
        val document: dev.mahin.backend.content.persistence.ContentDocumentEntity,
        val version: dev.mahin.backend.content.persistence.ContentVersionEntity,
    )

    fun listPregnancyWeeks(locale: String): PregnancyWeekListResponse {
        val now = Instant.now()
        val weeks =
            versionRepository
                .findPublishedPregnancyWeeks(locale)
                .mapNotNull { version ->
                    val document = documentRepository.findById(version.documentId).orElse(null)
                    if (document == null ||
                        !ContentPublicationRules.isPublishedOnCatalog(document, version, now) ||
                        isHiddenPregnancyWeekPlaceholder(version)
                    ) {
                        return@mapNotNull null
                    }
                    version.gestationalWeek
                }.distinct()
                .sorted()
        return PregnancyWeekListResponse(locale = locale, weeks = weeks)
    }

    @Transactional
    fun addBookmark(documentId: UUID) {
        val user = currentRegisteredUser()
        val document =
            documentRepository.findById(documentId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "article_not_found")
            }
        val versionId = document.publishedVersionId
        val version =
            versionId?.let { versionRepository.findById(it).orElse(null) }
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "article_not_found")
        if (!ContentPublicVisibility.isArticleEndpointEligible(document, version)) {
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
        val now = Instant.now()
        val bookmarks = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(user.userId)
        val summaries =
            bookmarks.mapNotNull { bookmark ->
                val document = documentRepository.findById(bookmark.documentId).orElse(null) ?: return@mapNotNull null
                val versionId = document.publishedVersionId ?: return@mapNotNull null
                val version = versionRepository.findById(versionId).orElse(null) ?: return@mapNotNull null
                if (!ContentPublicVisibility.isArticleEndpointEligible(document, version, now)) {
                    return@mapNotNull null
                }
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
        return ContentMapper.toArticleResponse(document, version, citationKeys, includeBody, objectMapper)
    }
}
