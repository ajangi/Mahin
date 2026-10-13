package dev.mahin.backend.content

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

object ContentWellnessQueryValidator {
    private val modes = setOf("cycle", "ttc", "pregnancy")
    private val cyclePhases = setOf("menstrual", "follicular", "ovulation", "luteal")
    private val programTypes = setOf("pelvic_floor", "movement", "breathing")
    private val collectionCategories =
        setOf(
            "cycle",
            "fertility",
            "pregnancy",
            "symptoms",
            "nutrition",
            "tests",
            "lifestyle",
        )

    fun validateDailyTipQuery(
        mode: String?,
        cyclePhase: String?,
        pregnancyWeek: Int?,
    ) {
        val code =
            when {
                mode != null && mode !in modes -> "invalid_daily_tip_mode"
                cyclePhase != null && cyclePhase !in cyclePhases -> "invalid_daily_tip_cycle_phase"
                pregnancyWeek != null && pregnancyWeek !in 1..42 -> "invalid_daily_tip_pregnancy_week"
                else -> null
            }
        if (code != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, code)
        }
    }

    fun validateProgramType(programType: String?) {
        if (programType != null && programType !in programTypes) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_program_type")
        }
    }

    fun validateCollectionCategory(category: String?) {
        if (category != null && category !in collectionCategories) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_collection_category")
        }
    }
}
