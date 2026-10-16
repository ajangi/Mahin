package dev.mahin.core.content

import kotlinx.serialization.Serializable

@Serializable
data class CareScheduleItemDto(
    val id: String,
    val title: String,
    val category: String,
    val weekStart: Int,
    val weekEnd: Int,
    val description: String? = null,
    val appointmentTypeHint: String? = null,
)

@Serializable
data class ContentCareScheduleDto(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String? = null,
    val placeholder: Boolean = false,
    val regionContext: String = "iran",
    val items: List<CareScheduleItemDto> = emptyList(),
)

@Serializable
data class ContentCareScheduleListResponseDto(
    val items: List<ContentCareScheduleSummaryDto>,
)

@Serializable
data class ContentCareScheduleSummaryDto(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String? = null,
    val placeholder: Boolean = false,
    val regionContext: String = "iran",
)

@Serializable
data class ChecklistItemTemplateDto(
    val id: String,
    val label: String,
    val section: String,
    val order: Int,
)

@Serializable
data class ContentChecklistTemplateDto(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String? = null,
    val placeholder: Boolean = false,
    val checklistKind: String = "hospital_bag",
    val sections: List<String> = emptyList(),
    val items: List<ChecklistItemTemplateDto> = emptyList(),
)

@Serializable
data class ContentChecklistTemplateListResponseDto(
    val items: List<ContentChecklistTemplateSummaryDto>,
)

@Serializable
data class ContentChecklistTemplateSummaryDto(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String? = null,
    val placeholder: Boolean = false,
    val checklistKind: String = "hospital_bag",
)
