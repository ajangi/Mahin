package dev.mahin.android.onboarding

import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate

data class OnboardingSetupFormState(
    val mode: ReproductiveMode,
    val lastPeriodStart: LocalDate,
    val typicalCycleLength: Int?,
    val typicalPeriodLength: Int?,
    val regularity: CycleRegularity,
    val isSaving: Boolean,
)
