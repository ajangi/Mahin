package dev.mahin.backend.content

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.content.persistence.ContentDocumentEntity
import dev.mahin.backend.content.persistence.ContentSourceEntity
import dev.mahin.backend.content.persistence.ContentVersionEntity
import java.time.format.DateTimeFormatter
import java.util.UUID

object ContentMapper {
    private val instantFormatter = DateTimeFormatter.ISO_INSTANT

    fun formatInstant(instant: java.time.Instant): String = instantFormatter.format(instant)

    fun toArticleResponse(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        sourceCitationKeys: List<String>,
        includeBody: Boolean,
        objectMapper: ObjectMapper? = null,
    ): ContentArticleResponse {
        val published = document.publishedVersionId == version.id && document.withdrawnAt == null
        val status =
            if (published) {
                ContentStatus.PUBLISHED
            } else {
                parseContentStatus(version.status)
            }
        val weekPayload =
            if (version.contentType == ContentTypes.PREGNANCY_WEEK && objectMapper != null) {
                ContentTypePayloadCodec.parsePregnancyWeek(objectMapper, version.typePayloadJson)
            } else {
                null
            }
        return ContentArticleResponse(
            id = document.id.toString(),
            slug = document.slug,
            locale = document.locale,
            title = version.title,
            summary = version.summary,
            body = if (includeBody && published) version.bodyRichtext else null,
            contentType = version.contentType,
            lifeStage = version.lifeStage,
            gestationalWeek = version.gestationalWeek,
            tags = parseTags(version.tagsJson),
            medicalRiskLevel = parseMedicalRiskLevel(version.medicalRiskLevel),
            status = status,
            contentVersion = version.versionNumber,
            sourceReferences = sourceCitationKeys,
            clinicalReviewer = version.clinicalReviewer,
            clinicalReviewedAt = version.clinicalReviewedAt?.let { instantFormatter.format(it) },
            nextReviewDueAt = version.nextReviewDueAt?.let { instantFormatter.format(it) },
            effectiveFrom = version.effectiveFrom?.let { instantFormatter.format(it) },
            effectiveTo = version.effectiveTo?.let { instantFormatter.format(it) },
            withdrawn = document.withdrawnAt != null,
            placeholder = weekPayload?.placeholder ?: false,
            fetalSummary = weekPayload?.fetalSummary,
            maternalSummary = weekPayload?.maternalSummary,
            sizeComparisonLabel = weekPayload?.sizeComparisonLabel,
            illustrationMediaAssetId = weekPayload?.illustrationMediaAssetId,
        )
    }

    fun toSourceResponse(entity: ContentSourceEntity): ContentSourceResponse =
        ContentSourceResponse(
            id = entity.id.toString(),
            citationKey = entity.citationKey,
            title = entity.title,
            url = entity.url,
            publicationDate = entity.publicationDate?.toString(),
            lastCheckedAt = entity.lastCheckedAt?.let { instantFormatter.format(it) },
            notes = entity.notes,
        )

    fun toAdminVersionResponse(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        sourceIds: List<UUID>,
        objectMapper: ObjectMapper,
    ): AdminContentVersionResponse =
        AdminContentVersionResponse(
            documentId = document.id.toString(),
            versionId = version.id.toString(),
            slug = document.slug,
            locale = document.locale,
            versionNumber = version.versionNumber,
            title = version.title,
            summary = version.summary,
            bodyRichtext = version.bodyRichtext,
            contentType = version.contentType,
            lifeStage = version.lifeStage,
            gestationalWeek = version.gestationalWeek,
            tags = parseTags(version.tagsJson),
            medicalRiskLevel = parseMedicalRiskLevel(version.medicalRiskLevel),
            status = parseContentStatus(version.status),
            reviewStage = version.reviewStage,
            sourceIds = sourceIds.map { it.toString() },
            clinicalReviewer = version.clinicalReviewer,
            clinicalReviewedAt = version.clinicalReviewedAt?.let { instantFormatter.format(it) },
            nextReviewDueAt = version.nextReviewDueAt?.let { instantFormatter.format(it) },
            published = document.publishedVersionId == version.id && document.withdrawnAt == null,
            typePayload = parseTypePayloadNode(objectMapper, version.typePayloadJson),
            effectiveFrom = version.effectiveFrom?.let { instantFormatter.format(it) },
            effectiveTo = version.effectiveTo?.let { instantFormatter.format(it) },
        )

    private fun parseTypePayloadNode(
        objectMapper: ObjectMapper,
        raw: String?,
    ): JsonNode? {
        if (raw.isNullOrBlank()) {
            return null
        }
        return runCatching { objectMapper.readTree(raw) }.getOrNull()
    }

    fun parseMedicalRiskLevel(value: String): MedicalRiskLevel =
        when (value) {
            "none" -> MedicalRiskLevel.NONE
            "general_education" -> MedicalRiskLevel.GENERAL_EDUCATION
            "contact_clinician" -> MedicalRiskLevel.CONTACT_CLINICIAN
            "urgent" -> MedicalRiskLevel.URGENT
            else -> MedicalRiskLevel.NONE
        }

    fun parseContentStatus(value: String): ContentStatus =
        when (value) {
            "draft" -> ContentStatus.DRAFT
            "review" -> ContentStatus.REVIEW
            "approved" -> ContentStatus.APPROVED
            "published" -> ContentStatus.PUBLISHED
            "retired" -> ContentStatus.RETIRED
            else -> ContentStatus.DRAFT
        }

    private fun parseTags(tagsJson: String?): List<String> {
        if (tagsJson.isNullOrBlank()) {
            return emptyList()
        }
        return runCatching {
            ObjectMapper().readValue(tagsJson, object : TypeReference<List<String>>() {})
        }.getOrDefault(emptyList())
    }
}
