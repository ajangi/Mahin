package dev.mahin.core.datetime

import java.time.LocalDate

/**
 * Resolves optional clinician/ultrasound EDD from UI state. When clinical entry is enabled but the
 * user has not changed the picker, the visible default (reference + 7 months) is persisted — never LMP.
 */
object PregnancyClinicalEddInput {
    fun defaultClinicalEddDate(referenceDate: LocalDate = LocalDate.now()): LocalDate = referenceDate.plusMonths(7)

    fun defaultClinicalEddJalali(referenceDate: LocalDate = LocalDate.now()): JalaliDate =
        PersianCivilDateConverter.toJalali(defaultClinicalEddDate(referenceDate))

    fun resolveClinicalEddGregorian(
        includeClinical: Boolean,
        selectedClinicalJalali: JalaliDate?,
        referenceDate: LocalDate = LocalDate.now(),
    ): LocalDate? {
        if (!includeClinical) return null
        val jalali = selectedClinicalJalali ?: defaultClinicalEddJalali(referenceDate)
        val gregorian = PersianCivilDateConverter.toGregorian(jalali)
        return gregorian
    }
}
