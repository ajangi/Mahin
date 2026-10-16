package dev.mahin.domain.pregnancy

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class PregnancyToolsCalculatorTest {
    @Test
    fun lmpCalculator_matchesDatingEngine() {
        val lmp = LocalDate.of(2025, 1, 1)
        val asOf = LocalDate.of(2025, 1, 15)
        val tool = PregnancyToolsCalculator.fromLmp(lmp, asOf)
        val status = PregnancyDatingEngineV1.status(lmp, null, asOf)
        assertThat(tool.effectiveEdd).isEqualTo(status.dating.effectiveEddDate)
        assertThat(tool.displayWeekNumber).isEqualTo(status.displayWeekNumber)
    }

    @Test
    fun ultrasoundCalculator_usesClinicalEdd() {
        val edd = LocalDate.of(2025, 10, 20)
        val asOf = LocalDate.of(2025, 7, 1)
        val tool = PregnancyToolsCalculator.fromUltrasoundEdd(edd, asOf)
        val lmp = edd.minusDays(280)
        val status = PregnancyDatingEngineV1.status(lmp, edd, asOf)
        assertThat(tool.effectiveEdd).isEqualTo(edd)
        assertThat(tool.gestationalAge.totalDays).isEqualTo(status.gestationalAge.totalDays)
    }

    @Test
    fun conceptionCalculator_adds266DaysToEdd() {
        val conception = LocalDate.of(2025, 2, 1)
        val tool = PregnancyToolsCalculator.fromConception(conception, conception.plusWeeks(10))
        assertThat(tool.effectiveEdd).isEqualTo(conception.plusDays(266))
    }
}
