package dev.mahin.backend.content

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.content.persistence.ContentDocumentEntity
import dev.mahin.backend.content.persistence.ContentDocumentRepository
import dev.mahin.backend.content.persistence.ContentSourceRepository
import dev.mahin.backend.content.persistence.ContentVersionEntity
import dev.mahin.backend.content.persistence.ContentVersionRepository
import dev.mahin.backend.content.persistence.ContentVersionSourceRepository
import java.time.Instant
import java.util.UUID
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ContentWellnessPublicService(
    private val documentRepository: ContentDocumentRepository,
    private val versionRepository: ContentVersionRepository,
    private val versionSourceRepository: ContentVersionSourceRepository,
    private val sourceRepository: ContentSourceRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${mahin.environment}") private val environment: String,
) {
    fun listDailyTips(
        locale: String,
        mode: String?,
        cyclePhase: String?,
        pregnancyWeek: Int?,
    ): ContentDailyTipListResponse {
        val now = Instant.now()
        val items =
            versionRepository
                .findPublishedByContentType(ContentTypes.DAILY_TIP, locale)
                .mapNotNull { version -> mapDailyTipIfVisible(version, mode, cyclePhase, pregnancyWeek, now) }
        return ContentDailyTipListResponse(items)
    }

    fun getDailyTip(documentId: UUID): ContentDailyTipResponse {
        val document = loadPublishedDocument(documentId, ContentTypes.DAILY_TIP)
        val version = loadPublishedVersion(document)
        val payload =
            ContentTypePayloadCodec.parseDailyTip(objectMapper, version.typePayloadJson)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "daily_tip_not_found")
        if (!isPayloadPublic(payload.placeholder)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "daily_tip_not_found")
        }
        return ContentWellnessMapper.toDailyTipResponse(
            document,
            version,
            payload,
            citationKeys(version),
        )
    }

    fun getDailyTipBySlug(
        slug: String,
        locale: String,
    ): ContentDailyTipResponse = getDailyTip(loadDocumentBySlug(slug, locale, ContentTypes.DAILY_TIP).id)

    fun listPrograms(
        locale: String,
        programType: String?,
    ): ContentProgramListResponse {
        val now = Instant.now()
        val items =
            versionRepository
                .findPublishedByContentType(ContentTypes.PROGRAM, locale)
                .mapNotNull { version ->
                    val document =
                        documentRepository.findById(version.documentId).orElse(null) ?: return@mapNotNull null
                    if (!ContentPublicationRules.isPublishedOnCatalog(document, version, now)) {
                        return@mapNotNull null
                    }
                    val payload =
                        ContentTypePayloadCodec.parseProgram(objectMapper, version.typePayloadJson)
                            ?: return@mapNotNull null
                    if (!isPayloadPublic(payload.placeholder)) {
                        return@mapNotNull null
                    }
                    if (programType != null && payload.programType != programType) {
                        return@mapNotNull null
                    }
                    ContentWellnessMapper.toProgramSummary(document, version, payload)
                }
        return ContentProgramListResponse(items)
    }

    fun getProgram(documentId: UUID): ContentProgramResponse {
        val document = loadPublishedDocument(documentId, ContentTypes.PROGRAM)
        val version = loadPublishedVersion(document)
        val payload =
            ContentTypePayloadCodec.parseProgram(objectMapper, version.typePayloadJson)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "program_not_found")
        if (!isPayloadPublic(payload.placeholder)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "program_not_found")
        }
        return ContentWellnessMapper.toProgramResponse(document, version, payload, citationKeys(version))
    }

    fun getProgramBySlug(
        slug: String,
        locale: String,
    ): ContentProgramResponse = getProgram(loadDocumentBySlug(slug, locale, ContentTypes.PROGRAM).id)

    fun listCollections(
        locale: String,
        category: String?,
    ): ContentCollectionListResponse {
        val now = Instant.now()
        val items =
            versionRepository
                .findPublishedByContentType(ContentTypes.COLLECTION, locale)
                .mapNotNull { version ->
                    val document =
                        documentRepository.findById(version.documentId).orElse(null) ?: return@mapNotNull null
                    if (!ContentPublicationRules.isPublishedOnCatalog(document, version, now)) {
                        return@mapNotNull null
                    }
                    val payload =
                        ContentTypePayloadCodec.parseCollection(objectMapper, version.typePayloadJson)
                            ?: return@mapNotNull null
                    if (!isPayloadPublic(payload.placeholder)) {
                        return@mapNotNull null
                    }
                    if (category != null && payload.category != category) {
                        return@mapNotNull null
                    }
                    ContentWellnessMapper.toCollectionSummary(document, version, payload)
                }
        return ContentCollectionListResponse(items)
    }

    fun getCollection(documentId: UUID): ContentCollectionResponse {
        val document = loadPublishedDocument(documentId, ContentTypes.COLLECTION)
        val version = loadPublishedVersion(document)
        val payload =
            ContentTypePayloadCodec.parseCollection(objectMapper, version.typePayloadJson)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "collection_not_found")
        if (!isPayloadPublic(payload.placeholder)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "collection_not_found")
        }
        val articles = resolveCollectionArticles(payload, document.locale)
        return ContentWellnessMapper.toCollectionResponse(
            document,
            version,
            payload,
            citationKeys(version),
            articles,
        )
    }

    fun getCollectionBySlug(
        slug: String,
        locale: String,
    ): ContentCollectionResponse = getCollection(loadDocumentBySlug(slug, locale, ContentTypes.COLLECTION).id)

    private fun mapDailyTipIfVisible(
        version: ContentVersionEntity,
        mode: String?,
        cyclePhase: String?,
        pregnancyWeek: Int?,
        now: Instant,
    ): ContentDailyTipResponse? {
        val document = documentRepository.findById(version.documentId).orElse(null)
        val payload = ContentTypePayloadCodec.parseDailyTip(objectMapper, version.typePayloadJson)
        val visible =
            document != null &&
                payload != null &&
                ContentPublicationRules.isPublishedOnCatalog(document, version, now) &&
                ContentPublicationRules.matchesDailyTipTargeting(payload, mode, cyclePhase, pregnancyWeek) &&
                isPayloadPublic(payload.placeholder)
        if (!visible || document == null || payload == null) {
            return null
        }
        return ContentWellnessMapper.toDailyTipResponse(
            document,
            version,
            payload,
            citationKeys(version),
        )
    }

    private fun resolveCollectionArticles(
        payload: CollectionPayload,
        locale: String,
    ): List<ContentArticleSummary> =
        payload.articleDocumentIds.mapNotNull { rawId ->
            val id = runCatching { UUID.fromString(rawId) }.getOrNull() ?: return@mapNotNull null
            val document = documentRepository.findById(id).orElse(null) ?: return@mapNotNull null
            if (document.locale != locale) {
                return@mapNotNull null
            }
            val versionId = document.publishedVersionId ?: return@mapNotNull null
            val version = versionRepository.findById(versionId).orElse(null) ?: return@mapNotNull null
            if (version.contentType != ContentTypes.ARTICLE ||
                !ContentPublicationRules.isPublishedOnCatalog(document, version)
            ) {
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

    private fun loadDocumentBySlug(
        slug: String,
        locale: String,
        contentType: String,
    ): ContentDocumentEntity {
        val document =
            documentRepository.findBySlugAndLocale(slug, locale).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "content_not_found")
            }
        val version = loadPublishedVersion(document)
        if (version.contentType != contentType) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "content_not_found")
        }
        return document
    }

    private fun loadPublishedDocument(
        documentId: UUID,
        contentType: String,
    ): ContentDocumentEntity {
        val document =
            documentRepository.findById(documentId).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "content_not_found")
            }
        val version = loadPublishedVersion(document)
        if (version.contentType != contentType) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "content_not_found")
        }
        return document
    }

    private fun loadPublishedVersion(document: ContentDocumentEntity): ContentVersionEntity {
        val publishedVersionId = document.publishedVersionId
        val version =
            if (document.withdrawnAt != null || publishedVersionId == null) {
                null
            } else {
                versionRepository.findById(publishedVersionId).orElse(null)
            }
        if (version == null || !ContentPublicationRules.isPublishedOnCatalog(document, version)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "content_not_found")
        }
        return version
    }

    private fun citationKeys(version: ContentVersionEntity): List<String> =
        versionSourceRepository.findByVersionId(version.id).mapNotNull { link ->
            sourceRepository.findById(link.sourceId).orElse(null)?.citationKey
        }

    private fun isPayloadPublic(placeholder: Boolean): Boolean =
        !ContentPublicVisibility.isPlaceholderHiddenInProduction(placeholder, environment)
}
