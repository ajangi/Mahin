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
class ContentPregnancyCompanionPublicService(
    private val documentRepository: ContentDocumentRepository,
    private val versionRepository: ContentVersionRepository,
    private val versionSourceRepository: ContentVersionSourceRepository,
    private val sourceRepository: ContentSourceRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${mahin.environment}") private val environment: String,
) {
    fun listCareSchedules(
        locale: String,
        gestationalWeek: Int?,
    ): ContentCareScheduleListResponse {
        if (gestationalWeek != null && gestationalWeek !in 1..42) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_gestational_week")
        }
        val now = Instant.now()
        val items =
            versionRepository
                .findPublishedByContentType(ContentTypes.CARE_SCHEDULE, locale)
                .mapNotNull { version -> mapCareScheduleIfVisible(version, gestationalWeek, now) }
        return ContentCareScheduleListResponse(items.map { it.summary })
    }

    fun getCareSchedule(documentId: UUID): ContentCareScheduleResponse {
        val mapped = loadCareSchedule(documentId)
        return mapped.detail
    }

    fun getCareScheduleBySlug(
        slug: String,
        locale: String,
    ): ContentCareScheduleResponse = getCareSchedule(loadDocumentBySlug(slug, locale, ContentTypes.CARE_SCHEDULE).id)

    fun listChecklistTemplates(
        locale: String,
        checklistKind: String?,
    ): ContentChecklistTemplateListResponse {
        if (checklistKind != null && checklistKind != "hospital_bag") {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_checklist_kind")
        }
        val now = Instant.now()
        val items =
            versionRepository
                .findPublishedByContentType(ContentTypes.CHECKLIST_TEMPLATE, locale)
                .mapNotNull { version -> mapChecklistIfVisible(version, checklistKind, now) }
        return ContentChecklistTemplateListResponse(items.map { it.summary })
    }

    fun getChecklistTemplate(documentId: UUID): ContentChecklistTemplateResponse {
        val mapped = loadChecklistTemplate(documentId)
        return mapped.detail
    }

    fun getChecklistTemplateBySlug(
        slug: String,
        locale: String,
    ): ContentChecklistTemplateResponse =
        getChecklistTemplate(loadDocumentBySlug(slug, locale, ContentTypes.CHECKLIST_TEMPLATE).id)

    private data class CareScheduleMapped(
        val summary: ContentCareScheduleSummary,
        val detail: ContentCareScheduleResponse,
    )

    private data class ChecklistMapped(
        val summary: ContentChecklistTemplateSummary,
        val detail: ContentChecklistTemplateResponse,
    )

    private fun mapCareScheduleIfVisible(
        version: ContentVersionEntity,
        gestationalWeek: Int?,
        now: Instant,
    ): CareScheduleMapped? {
        val document = documentRepository.findById(version.documentId).orElse(null)
        val payload = ContentTypePayloadCodec.parseCareSchedule(objectMapper, version.typePayloadJson)
        val weekMatches =
            gestationalWeek == null ||
                payload?.items?.any { gestationalWeek in it.weekStart..it.weekEnd } == true
        val visible =
            document != null &&
                payload != null &&
                ContentPublicationRules.isPublishedOnCatalog(document, version, now) &&
                isPayloadPublic(payload.placeholder) &&
                weekMatches
        if (!visible || document == null || payload == null) {
            return null
        }
        val citations = citationKeys(version)
        return CareScheduleMapped(
            summary = ContentPregnancyCompanionMapper.toCareScheduleSummary(document, version, payload),
            detail = ContentPregnancyCompanionMapper.toCareScheduleResponse(document, version, payload, citations),
        )
    }

    private fun mapChecklistIfVisible(
        version: ContentVersionEntity,
        checklistKind: String?,
        now: Instant,
    ): ChecklistMapped? {
        val document = documentRepository.findById(version.documentId).orElse(null)
        val payload = ContentTypePayloadCodec.parseChecklistTemplate(objectMapper, version.typePayloadJson)
        val kindMatches = checklistKind == null || payload?.checklistKind == checklistKind
        val visible =
            document != null &&
                payload != null &&
                ContentPublicationRules.isPublishedOnCatalog(document, version, now) &&
                isPayloadPublic(payload.placeholder) &&
                kindMatches
        if (!visible || document == null || payload == null) {
            return null
        }
        val citations = citationKeys(version)
        return ChecklistMapped(
            summary = ContentPregnancyCompanionMapper.toChecklistTemplateSummary(document, version, payload),
            detail = ContentPregnancyCompanionMapper.toChecklistTemplateResponse(document, version, payload, citations),
        )
    }

    private fun loadCareSchedule(documentId: UUID): CareScheduleMapped {
        val document = loadPublishedDocument(documentId, ContentTypes.CARE_SCHEDULE)
        val version = loadPublishedVersion(document)
        val payload =
            ContentTypePayloadCodec.parseCareSchedule(objectMapper, version.typePayloadJson)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "care_schedule_not_found")
        if (!isPayloadPublic(payload.placeholder)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "care_schedule_not_found")
        }
        val citations = citationKeys(version)
        return CareScheduleMapped(
            summary = ContentPregnancyCompanionMapper.toCareScheduleSummary(document, version, payload),
            detail = ContentPregnancyCompanionMapper.toCareScheduleResponse(document, version, payload, citations),
        )
    }

    private fun loadChecklistTemplate(documentId: UUID): ChecklistMapped {
        val document = loadPublishedDocument(documentId, ContentTypes.CHECKLIST_TEMPLATE)
        val version = loadPublishedVersion(document)
        val payload =
            ContentTypePayloadCodec.parseChecklistTemplate(objectMapper, version.typePayloadJson)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "checklist_template_not_found")
        if (!isPayloadPublic(payload.placeholder)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "checklist_template_not_found")
        }
        val citations = citationKeys(version)
        return ChecklistMapped(
            summary = ContentPregnancyCompanionMapper.toChecklistTemplateSummary(document, version, payload),
            detail = ContentPregnancyCompanionMapper.toChecklistTemplateResponse(document, version, payload, citations),
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
