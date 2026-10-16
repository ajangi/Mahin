package dev.mahin.backend.content

import dev.mahin.backend.content.persistence.ContentDocumentEntity
import dev.mahin.backend.content.persistence.ContentVersionEntity

object ContentPregnancyCompanionMapper {
    fun toCareScheduleResponse(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        payload: CareSchedulePayload,
        sourceCitationKeys: List<String>,
    ): ContentCareScheduleResponse =
        ContentCareScheduleResponse(
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
            effectiveFrom = version.effectiveFrom?.let { ContentMapper.formatInstant(it) },
            effectiveTo = version.effectiveTo?.let { ContentMapper.formatInstant(it) },
            placeholder = payload.placeholder,
            regionContext = payload.regionContext,
            items = payload.items,
        )

    fun toCareScheduleSummary(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        payload: CareSchedulePayload,
    ): ContentCareScheduleSummary =
        ContentCareScheduleSummary(
            id = document.id.toString(),
            slug = document.slug,
            locale = document.locale,
            title = version.title,
            summary = version.summary,
            placeholder = payload.placeholder,
            regionContext = payload.regionContext,
        )

    fun toChecklistTemplateResponse(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        payload: ChecklistTemplatePayload,
        sourceCitationKeys: List<String>,
    ): ContentChecklistTemplateResponse =
        ContentChecklistTemplateResponse(
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
            effectiveFrom = version.effectiveFrom?.let { ContentMapper.formatInstant(it) },
            effectiveTo = version.effectiveTo?.let { ContentMapper.formatInstant(it) },
            placeholder = payload.placeholder,
            checklistKind = payload.checklistKind,
            sections = payload.sections,
            items = payload.items.sortedWith(compareBy({ it.section }, { it.order })),
        )

    fun toChecklistTemplateSummary(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        payload: ChecklistTemplatePayload,
    ): ContentChecklistTemplateSummary =
        ContentChecklistTemplateSummary(
            id = document.id.toString(),
            slug = document.slug,
            locale = document.locale,
            title = version.title,
            summary = version.summary,
            placeholder = payload.placeholder,
            checklistKind = payload.checklistKind,
        )

    fun toWarningSignsResponse(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        payload: WarningSignsPayload,
        sourceCitationKeys: List<String>,
    ): ContentWarningSignsResponse =
        ContentWarningSignsResponse(
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
            effectiveFrom = version.effectiveFrom?.let { ContentMapper.formatInstant(it) },
            effectiveTo = version.effectiveTo?.let { ContentMapper.formatInstant(it) },
            placeholder = payload.placeholder,
            introTitle = payload.introTitle,
            introBody = payload.introBody,
            signs = payload.signs,
            escalationTitle = payload.escalationTitle,
            escalationBody = payload.escalationBody,
            contactClinicianLabel = payload.contactClinicianLabel,
            bpThresholds = payload.bpThresholds,
        )

    fun toWarningSignsSummary(
        document: ContentDocumentEntity,
        version: ContentVersionEntity,
        payload: WarningSignsPayload,
    ): ContentWarningSignsSummary =
        ContentWarningSignsSummary(
            id = document.id.toString(),
            slug = document.slug,
            locale = document.locale,
            title = version.title,
            summary = version.summary,
            placeholder = payload.placeholder,
        )
}
