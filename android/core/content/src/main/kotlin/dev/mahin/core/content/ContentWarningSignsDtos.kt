package dev.mahin.core.content

import kotlinx.serialization.Serializable

@Serializable
data class ContentWarningSignsListResponseDto(
    val items: List<ContentWarningSignsSummaryDto>,
)

@Serializable
data class ContentWarningSignsSummaryDto(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String? = null,
    val placeholder: Boolean = false,
)

@Serializable
data class ContentWarningSignsDto(
    val id: String,
    val slug: String,
    val locale: String,
    val title: String,
    val summary: String? = null,
    val placeholder: Boolean = false,
    val introTitle: String,
    val introBody: String,
    val signs: List<ContentWarningSignItemDto> = emptyList(),
    val escalationTitle: String,
    val escalationBody: String,
    val contactClinicianLabel: String,
    val bpThresholds: ContentPregnancyBpThresholdsDto,
)

@Serializable
data class ContentWarningSignItemDto(
    val id: String,
    val title: String,
    val summary: String,
)

@Serializable
data class ContentPregnancyBpThresholdsDto(
    val elevatedSystolic: Int,
    val elevatedDiastolic: Int,
    val urgentSystolic: Int,
    val urgentDiastolic: Int,
)
