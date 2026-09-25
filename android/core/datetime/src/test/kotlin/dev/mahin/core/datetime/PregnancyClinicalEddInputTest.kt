package dev.mahin.core.datetime

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class PregnancyClinicalEddInputTest {
    @Test
    fun includeClinical_withoutPickerTouch_storesDefaultNotLmp() {
        val reference = LocalDate.of(2025, 3, 1)
        val lmp = LocalDate.of(2024, 6, 1)
        val clinical =
            PregnancyClinicalEddInput.resolveClinicalEddGregorian(
                includeClinical = true,
                selectedClinicalJalali = null,
                referenceDate = reference,
            )
        assertThat(clinical).isNotNull()
        assertThat(clinical).isNotEqualTo(lmp)
        assertThat(clinical).isEqualTo(PregnancyClinicalEddInput.defaultClinicalEddDate(reference))
    }

    @Test
    fun includeClinical_off_returnsNull() {
        assertThat(
            PregnancyClinicalEddInput.resolveClinicalEddGregorian(
                includeClinical = false,
                selectedClinicalJalali = null,
            ),
        ).isNull()
    }
}
