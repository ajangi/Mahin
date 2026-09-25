package dev.mahin.domain.pregnancy

import dev.mahin.core.model.PregnancyDatingSource
import java.time.LocalDate

data class PregnancyDatingSnapshot(
    val lmpDate: LocalDate,
    val lmpBasedEdd: LocalDate,
    val clinicalEddDate: LocalDate?,
    val effectiveEddDate: LocalDate,
    val datingSource: PregnancyDatingSource,
)

data class GestationalAge(
    val weeks: Int,
    val days: Int,
    val totalDays: Int,
)

enum class PregnancyTrimester {
    FIRST,
    SECOND,
    THIRD,
}

data class PregnancyStatusSnapshot(
    val dating: PregnancyDatingSnapshot,
    val gestationalAge: GestationalAge,
    val trimester: PregnancyTrimester,
    val daysUntilEdd: Long,
    val displayWeekNumber: Int,
)
