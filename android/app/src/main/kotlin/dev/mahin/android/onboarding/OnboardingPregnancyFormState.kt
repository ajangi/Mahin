package dev.mahin.android.onboarding

import dev.mahin.core.datetime.JalaliDate

data class OnboardingPregnancyFormState(
    val lmpJalali: JalaliDate,
    val includeClinicalEdd: Boolean,
    val clinicalEddJalali: JalaliDate?,
    val isSaving: Boolean,
)
