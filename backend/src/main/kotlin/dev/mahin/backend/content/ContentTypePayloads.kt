package dev.mahin.backend.content

import com.fasterxml.jackson.annotation.JsonProperty

data class DailyTipTargeting(
    val modes: List<String> = emptyList(),
    val cyclePhases: List<String> = emptyList(),
    val pregnancyWeeks: List<Int> = emptyList(),
)

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

data class ProgramStepPayload(
    val instruction: String,
    val durationSeconds: Int? = null,
    val reps: Int? = null,
    val restSeconds: Int? = null,
    val mediaAssetId: String? = null,
)

data class ProgramSessionPayload(
    val id: String,
    val title: String,
    val order: Int,
    val steps: List<ProgramStepPayload>,
)

data class ProgramPayload(
    val placeholder: Boolean = false,
    val programType: String,
    val safetyNotice: String? = null,
    val contraindications: String? = null,
    val essentialSafetyInformation: String? = null,
    val sessions: List<ProgramSessionPayload> = emptyList(),
)

data class CollectionPayload(
    val placeholder: Boolean = false,
    val category: String,
    val heroMediaAssetId: String? = null,
    val articleDocumentIds: List<String> = emptyList(),
    val featured: Boolean = false,
)
