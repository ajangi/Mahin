package dev.mahin.backend.content

data class ContentDailyTipResponse(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String?,
    val body: String?,
    val medicalRiskLevel: MedicalRiskLevel,
    val contentVersion: Int,
    val sourceReferences: List<String>,
    val clinicalReviewer: String?,
    val clinicalReviewedAt: String?,
    val effectiveFrom: String?,
    val effectiveTo: String?,
    val placeholder: Boolean,
    val targeting: DailyTipTargeting,
)

data class ContentDailyTipListResponse(
    val items: List<ContentDailyTipResponse>,
)

data class ContentProgramResponse(
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
    val placeholder: Boolean,
    val programType: String,
    val safetyNotice: String?,
    val contraindications: String?,
    val essentialSafetyInformation: String?,
    val sessions: List<ProgramSessionPayload>,
)

data class ContentProgramListResponse(
    val items: List<ContentProgramSummary>,
)

data class ContentProgramSummary(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String?,
    val programType: String,
    val placeholder: Boolean,
)

data class ContentCollectionResponse(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String?,
    val medicalRiskLevel: MedicalRiskLevel,
    val contentVersion: Int,
    val sourceReferences: List<String>,
    val placeholder: Boolean,
    val category: String,
    val heroMediaAssetId: String?,
    val featured: Boolean,
    val articles: List<ContentArticleSummary>,
)

data class ContentCollectionListResponse(
    val items: List<ContentCollectionSummary>,
)

data class ContentCollectionSummary(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String?,
    val category: String,
    val featured: Boolean,
    val placeholder: Boolean,
)
