package dev.mahin.domain.pregnancy

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import org.junit.Test

class PregnancyDatingEngineV1Test {
    @Test
    fun lmpBasedEdd_is280DaysAfterLmp() {
        val lmp = LocalDate.of(2025, 1, 1)
        assertThat(PregnancyDatingEngineV1.lmpBasedEdd(lmp)).isEqualTo(LocalDate.of(2025, 10, 8))
    }

    @Test
    fun clinicalEdd_supersedesEffectiveEdd() {
        val lmp = LocalDate.of(2025, 1, 1)
        val clinical = LocalDate.of(2025, 10, 15)
        val dating = PregnancyDatingEngineV1.resolveDating(lmp, clinical)
        assertThat(dating.effectiveEddDate).isEqualTo(clinical)
        assertThat(dating.datingSource.name).isEqualTo("CLINICAL_OR_ULTRASOUND")
    }

    @Test
    fun gestationalAge_fromLmp_countsDaysSinceLmp() {
        val lmp = LocalDate.of(2025, 1, 1)
        val dating = PregnancyDatingEngineV1.resolveDating(lmp, null)
        val ga = PregnancyDatingEngineV1.gestationalAge(dating, LocalDate.of(2025, 1, 15))
        assertThat(ga.totalDays).isEqualTo(14)
        assertThat(ga.weeks).isEqualTo(2)
        assertThat(ga.days).isEqualTo(0)
    }

    @Test
    fun gestationalAge_fromClinicalEdd_usesDaysRemaining() {
        val lmp = LocalDate.of(2025, 1, 1)
        val clinical = LocalDate.of(2025, 10, 20)
        val dating = PregnancyDatingEngineV1.resolveDating(lmp, clinical)
        val asOf = LocalDate.of(2025, 7, 1)
        val ga = PregnancyDatingEngineV1.gestationalAge(dating, asOf)
        assertThat(dating.datingSource.name).isEqualTo("CLINICAL_OR_ULTRASOUND")
        assertThat(ga.totalDays).isEqualTo((280 - ChronoUnit.DAYS.between(asOf, clinical)).toInt())
    }

    @Test
    fun displayWeekNumber_isOneBased() {
        val status =
            PregnancyDatingEngineV1.status(
                lmpDate = LocalDate.of(2025, 1, 1),
                clinicalEddDate = null,
                asOfDate = LocalDate.of(2025, 1, 8),
            )
        assertThat(status.displayWeekNumber).isEqualTo(2)
        assertThat(status.trimester).isEqualTo(PregnancyTrimester.FIRST)
    }
}
