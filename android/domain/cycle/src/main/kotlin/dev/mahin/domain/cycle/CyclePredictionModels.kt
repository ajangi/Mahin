package dev.mahin.domain.cycle

import dev.mahin.core.model.CycleRegularity
import java.time.LocalDate

data class CompletedCycleSpan(
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val nextPeriodStart: LocalDate?,
)

data class CyclePredictionInput(
    val today: LocalDate,
    val completedCycles: List<CompletedCycleSpan>,
    val openPeriodStart: LocalDate?,
    val openPeriodEnd: LocalDate?,
    val typicalCycleLengthDays: Int?,
    val typicalPeriodLengthDays: Int?,
    val regularity: CycleRegularity,
)

data class DateRangeEstimate(
    val earliest: LocalDate,
    val latest: LocalDate,
)

data class CyclePredictionResult(
    val algorithmVersion: String,
    val confidence: PredictionConfidence,
    val cycleDay: Int?,
    val nextPeriod: DateRangeEstimate?,
    val fertileWindow: DateRangeEstimate?,
    val estimatedOvulation: DateRangeEstimate?,
    val insufficientDataReason: String?,
)
