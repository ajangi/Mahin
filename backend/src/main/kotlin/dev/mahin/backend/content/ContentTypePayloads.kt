package dev.mahin.backend.content

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = false)
data class DailyTipTargeting(
    val modes: List<String> = emptyList(),
    val cyclePhases: List<String> = emptyList(),
    val pregnancyWeeks: List<Int> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class DailyTipPayload(
    val placeholder: Boolean = false,
    val targeting: DailyTipTargeting = DailyTipTargeting(),
)

enum class ProgramKind {
    @JsonProperty("pelvic_floor")
    PELVIC_FLOOR,

    @JsonProperty("movement")
    MOVEMENT,

    @JsonProperty("breathing")
    BREATHING,
    ;

    fun wireValue(): String =
        when (this) {
            PELVIC_FLOOR -> "pelvic_floor"
            MOVEMENT -> "movement"
            BREATHING -> "breathing"
        }
}

@JsonIgnoreProperties(ignoreUnknown = false)
data class ProgramStepPayload(
    val instruction: String,
    val durationSeconds: Int? = null,
    val reps: Int? = null,
    val restSeconds: Int? = null,
    val mediaAssetId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class ProgramSessionPayload(
    val id: String,
    val title: String,
    val order: Int,
    val steps: List<ProgramStepPayload>,
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class ProgramPayload(
    val placeholder: Boolean = false,
    val programType: String,
    val safetyNotice: String? = null,
    val contraindications: String? = null,
    val essentialSafetyInformation: String? = null,
    val sessions: List<ProgramSessionPayload> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class CollectionPayload(
    val placeholder: Boolean = false,
    val category: String,
    val heroMediaAssetId: String? = null,
    val articleDocumentIds: List<String> = emptyList(),
    val featured: Boolean = false,
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class PregnancyWeekPayload(
    val placeholder: Boolean = false,
    val fetalSummary: String? = null,
    val maternalSummary: String? = null,
    val sizeComparisonLabel: String? = null,
    val illustrationMediaAssetId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class CareScheduleItemPayload(
    val id: String,
    val title: String,
    val category: String,
    val weekStart: Int,
    val weekEnd: Int,
    val description: String? = null,
    val appointmentTypeHint: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class CareSchedulePayload(
    val placeholder: Boolean = false,
    val regionContext: String = "iran",
    val items: List<CareScheduleItemPayload> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class ChecklistItemTemplatePayload(
    val id: String,
    val label: String,
    val section: String,
    val order: Int,
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class ChecklistTemplatePayload(
    val placeholder: Boolean = false,
    val checklistKind: String = "hospital_bag",
    val sections: List<String> = emptyList(),
    val items: List<ChecklistItemTemplatePayload> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class WarningSignItemPayload(
    val id: String,
    val title: String,
    val summary: String,
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class PregnancyBpThresholdsPayload(
    val elevatedSystolic: Int,
    val elevatedDiastolic: Int,
    val urgentSystolic: Int,
    val urgentDiastolic: Int,
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class WarningSignsPayload(
    val placeholder: Boolean = false,
    val introTitle: String,
    val introBody: String,
    val signs: List<WarningSignItemPayload> = emptyList(),
    val escalationTitle: String,
    val escalationBody: String,
    val contactClinicianLabel: String,
    val bpThresholds: PregnancyBpThresholdsPayload,
)
