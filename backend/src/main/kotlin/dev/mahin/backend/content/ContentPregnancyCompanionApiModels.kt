package dev.mahin.backend.content

data class ContentCareScheduleResponse(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String?,
    val medicalRiskLevel: MedicalRiskLevel,
    val contentVersion: Int,
    val sourceReferences: List<String>,
    val clinicalReviewer: String?,
    val clinicalReviewedAt: String?,
    val effectiveFrom: String?,
    val effectiveTo: String?,
    val placeholder: Boolean,
    val regionContext: String,
    val items: List<CareScheduleItemPayload>,
)

data class ContentCareScheduleListResponse(
    val items: List<ContentCareScheduleSummary>,
)

data class ContentCareScheduleSummary(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String?,
    val placeholder: Boolean,
    val regionContext: String,
)

data class ContentChecklistTemplateResponse(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String?,
    val medicalRiskLevel: MedicalRiskLevel,
    val contentVersion: Int,
    val sourceReferences: List<String>,
    val clinicalReviewer: String?,
    val clinicalReviewedAt: String?,
    val effectiveFrom: String?,
    val effectiveTo: String?,
    val placeholder: Boolean,
    val checklistKind: String,
    val sections: List<String>,
    val items: List<ChecklistItemTemplatePayload>,
)

data class ContentChecklistTemplateListResponse(
    val items: List<ContentChecklistTemplateSummary>,
)

data class ContentChecklistTemplateSummary(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String?,
    val placeholder: Boolean,
    val checklistKind: String,
)

data class ContentWarningSignsResponse(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String?,
    val medicalRiskLevel: MedicalRiskLevel,
    val contentVersion: Int,
    val sourceReferences: List<String>,
    val clinicalReviewer: String?,
    val clinicalReviewedAt: String?,
    val effectiveFrom: String?,
    val effectiveTo: String?,
    val placeholder: Boolean,
    val introTitle: String,
    val introBody: String,
    val signs: List<WarningSignItemPayload>,
    val escalationTitle: String,
    val escalationBody: String,
    val contactClinicianLabel: String,
    val bpThresholds: PregnancyBpThresholdsPayload,
)

data class ContentWarningSignsListResponse(
    val items: List<ContentWarningSignsSummary>,
)

data class ContentWarningSignsSummary(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String?,
    val placeholder: Boolean,
)
