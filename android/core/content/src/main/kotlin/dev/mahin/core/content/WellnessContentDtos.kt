package dev.mahin.core.content

import kotlinx.serialization.Serializable

@Serializable
data class ContentDailyTipListResponseDto(
    val items: List<ContentDailyTipDto>,
)

@Serializable
data class ContentDailyTipDto(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String? = null,
    val body: String? = null,
    val medicalRiskLevel: String,
    val contentVersion: Int,
    val sourceReferences: List<String> = emptyList(),
    val clinicalReviewer: String? = null,
    val clinicalReviewedAt: String? = null,
    val effectiveFrom: String? = null,
    val effectiveTo: String? = null,
    val placeholder: Boolean = false,
    val targeting: DailyTipTargetingDto = DailyTipTargetingDto(),
)

@Serializable
data class DailyTipTargetingDto(
    val modes: List<String> = emptyList(),
    val cyclePhases: List<String> = emptyList(),
    val pregnancyWeeks: List<Int> = emptyList(),
)

@Serializable
data class ContentProgramListResponseDto(
    val items: List<ContentProgramSummaryDto>,
)

@Serializable
data class ContentProgramSummaryDto(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String? = null,
    val programType: String,
    val placeholder: Boolean = false,
)

@Serializable
data class ContentProgramDto(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String? = null,
    val programType: String,
    val placeholder: Boolean = false,
    val medicalRiskLevel: String,
    val contentVersion: Int,
    val sourceReferences: List<String> = emptyList(),
    val clinicalReviewer: String? = null,
    val clinicalReviewedAt: String? = null,
    val safetyNotice: String? = null,
    val contraindications: String? = null,
    val essentialSafetyInformation: String? = null,
    val sessions: List<ProgramSessionDto> = emptyList(),
)

@Serializable
data class ProgramSessionDto(
    val id: String,
    val title: String,
    val order: Int,
    val steps: List<ProgramStepDto>,
)

@Serializable
data class ProgramStepDto(
    val instruction: String,
    val durationSeconds: Int? = null,
    val reps: Int? = null,
    val restSeconds: Int? = null,
    val mediaAssetId: String? = null,
)

@Serializable
data class ContentCollectionListResponseDto(
    val items: List<ContentCollectionSummaryDto>,
)

@Serializable
data class ContentCollectionSummaryDto(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String? = null,
    val category: String,
    val featured: Boolean = false,
    val placeholder: Boolean = false,
)

@Serializable
data class ContentCollectionDto(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String? = null,
    val category: String,
    val featured: Boolean = false,
    val placeholder: Boolean = false,
    val medicalRiskLevel: String,
    val contentVersion: Int,
    val sourceReferences: List<String> = emptyList(),
    val heroMediaAssetId: String? = null,
    val articles: List<ContentArticleSummaryDto> = emptyList(),
)

@Serializable
data class MediaAssetDto(
    val id: String,
    val storageKey: String,
    val family: String,
    val version: Int,
    val locale: String,
    val approvalStatus: String,
    val medicalGoverned: Boolean,
    val altText: String,
    val publicUrl: String? = null,
)
