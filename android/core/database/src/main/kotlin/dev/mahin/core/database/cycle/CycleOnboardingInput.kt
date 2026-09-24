package dev.mahin.core.database.cycle

import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate

data class CycleOnboardingInput(
    val mode: ReproductiveMode,
    val lastPeriodStart: LocalDate,
    val lastPeriodEnd: LocalDate?,
    val typicalCycleLengthDays: Int?,
    val typicalPeriodLengthDays: Int?,
    val regularity: CycleRegularity,
    val priorPeriodStarts: List<LocalDate> = emptyList(),
)
