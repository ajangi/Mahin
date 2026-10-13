package dev.mahin.backend.content

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mahin.backend.content.persistence.ContentDocumentEntity
import dev.mahin.backend.content.persistence.ContentVersionEntity

object ContentWellnessMapper {
    fun toDailyTipResponse(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        payload: DailyTipPayload,
        sourceCitationKeys: List<String>,
    ): ContentDailyTipResponse =
        ContentDailyTipResponse(
            id = document.id.toString(),
            slug = document.slug,
            locale = document.locale,
            title = version.title,
            summary = version.summary,
            body = version.bodyRichtext,
            medicalRiskLevel = ContentMapper.parseMedicalRiskLevel(version.medicalRiskLevel),
            contentVersion = version.versionNumber,
            sourceReferences = sourceCitationKeys,
            clinicalReviewer = version.clinicalReviewer,
            clinicalReviewedAt = version.clinicalReviewedAt?.let { ContentMapper.formatInstant(it) },
            effectiveFrom = version.effectiveFrom?.let { ContentMapper.formatInstant(it) },
            effectiveTo = version.effectiveTo?.let { ContentMapper.formatInstant(it) },
            placeholder = payload.placeholder,
            targeting = payload.targeting,
        )

    fun toProgramResponse(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        payload: ProgramPayload,
        sourceCitationKeys: List<String>,
    ): ContentProgramResponse =
        ContentProgramResponse(
            id = document.id.toString(),
            slug = document.slug,
            locale = document.locale,
            title = version.title,
            summary = version.summary,
            medicalRiskLevel = ContentMapper.parseMedicalRiskLevel(version.medicalRiskLevel),
            contentVersion = version.versionNumber,
            sourceReferences = sourceCitationKeys,
            clinicalReviewer = version.clinicalReviewer,
            clinicalReviewedAt = version.clinicalReviewedAt?.let { ContentMapper.formatInstant(it) },
            placeholder = payload.placeholder,
            programType = payload.programType,
            safetyNotice = payload.safetyNotice,
            contraindications = payload.contraindications,
            essentialSafetyInformation = payload.essentialSafetyInformation,
            sessions = payload.sessions.sortedBy { it.order },
        )

    fun toProgramSummary(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        payload: ProgramPayload,
    ): ContentProgramSummary =
        ContentProgramSummary(
            id = document.id.toString(),
            slug = document.slug,
            locale = document.locale,
            title = version.title,
            summary = version.summary,
            programType = payload.programType,
            placeholder = payload.placeholder,
        )

    fun toCollectionResponse(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        payload: CollectionPayload,
        sourceCitationKeys: List<String>,
        articles: List<ContentArticleSummary>,
    ): ContentCollectionResponse =
        ContentCollectionResponse(
            id = document.id.toString(),
            slug = document.slug,
            locale = document.locale,
            title = version.title,
            summary = version.summary,
            medicalRiskLevel = ContentMapper.parseMedicalRiskLevel(version.medicalRiskLevel),
            contentVersion = version.versionNumber,
            sourceReferences = sourceCitationKeys,
            placeholder = payload.placeholder,
            category = payload.category,
            heroMediaAssetId = payload.heroMediaAssetId,
            featured = payload.featured,
            articles = articles,
        )

    fun toCollectionSummary(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        payload: CollectionPayload,
    ): ContentCollectionSummary =
        ContentCollectionSummary(
            id = document.id.toString(),
            slug = document.slug,
            locale = document.locale,
            title = version.title,
            summary = version.summary,
            category = payload.category,
            featured = payload.featured,
            placeholder = payload.placeholder,
        )

    fun parsePayload(
        mapper: ObjectMapper,
        version: ContentVersionEntity,
    ): Any =
        when (version.contentType) {
            ContentTypes.DAILY_TIP ->
                ContentTypePayloadCodec.parseDailyTip(mapper, version.typePayloadJson)
                    ?: error("missing_daily_tip_payload")
            ContentTypes.PROGRAM ->
                ContentTypePayloadCodec.parseProgram(mapper, version.typePayloadJson)
                    ?: error("missing_program_payload")
            ContentTypes.COLLECTION ->
                ContentTypePayloadCodec.parseCollection(mapper, version.typePayloadJson)
                    ?: error("missing_collection_payload")
            else -> error("not_wellness_type")
        }
}
