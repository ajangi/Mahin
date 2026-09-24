package dev.mahin.domain.cycle

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.CycleRegularity
import java.time.LocalDate
import org.junit.Test

class CyclePredictionEngineV1Test {
    @Test
    fun insufficientData_whenNoPeriodLogged() {
        val result =
            CyclePredictionEngineV1.predict(
                CyclePredictionInput(
                    today = LocalDate.of(2025, 6, 15),
                    completedCycles = emptyList(),
                    openPeriodStart = null,
                    openPeriodEnd = null,
                    typicalCycleLengthDays = null,
                    typicalPeriodLengthDays = null,
                    regularity = CycleRegularity.UNKNOWN,
                ),
            )
        assertThat(result.confidence).isEqualTo(PredictionConfidence.INSUFFICIENT_DATA)
        assertThat(result.nextPeriod).isNull()
        assertThat(result.fertileWindow).isNull()
    }

    @Test
    fun insufficientData_withAnchorOnly_doesNotEmitEstimateRanges() {
        val result =
            CyclePredictionEngineV1.predict(
                CyclePredictionInput(
                    today = LocalDate.of(2025, 6, 10),
                    completedCycles = emptyList(),
                    openPeriodStart = LocalDate.of(2025, 6, 1),
                    openPeriodEnd = null,
                    typicalCycleLengthDays = null,
                    typicalPeriodLengthDays = null,
                    regularity = CycleRegularity.UNKNOWN,
                ),
            )
        assertThat(result.confidence).isEqualTo(PredictionConfidence.INSUFFICIENT_DATA)
        assertThat(result.cycleDay).isEqualTo(10)
        assertThat(result.nextPeriod).isNull()
        assertThat(result.fertileWindow).isNull()
        assertThat(result.estimatedOvulation).isNull()
    }

    @Test
    fun predictsNextPeriodRange_fromTwoCompletedCycles() {
        val cycles =
            listOf(
                CompletedCycleSpan(
                    periodStart = LocalDate.of(2025, 4, 1),
                    periodEnd = LocalDate.of(2025, 4, 5),
                    nextPeriodStart = LocalDate.of(2025, 5, 1),
                ),
                CompletedCycleSpan(
                    periodStart = LocalDate.of(2025, 5, 1),
                    periodEnd = LocalDate.of(2025, 5, 5),
                    nextPeriodStart = LocalDate.of(2025, 6, 1),
                ),
            )
        val result =
            CyclePredictionEngineV1.predict(
                CyclePredictionInput(
                    today = LocalDate.of(2025, 6, 10),
                    completedCycles = cycles,
                    openPeriodStart = LocalDate.of(2025, 6, 1),
                    openPeriodEnd = LocalDate.of(2025, 6, 5),
                    typicalCycleLengthDays = null,
                    typicalPeriodLengthDays = null,
                    regularity = CycleRegularity.REGULAR,
                ),
            )
        assertThat(result.algorithmVersion).isEqualTo(CycleDomainModule.PREDICTION_ALGORITHM_VERSION)
        assertThat(result.confidence).isEqualTo(PredictionConfidence.MEDIUM)
        assertThat(result.cycleDay).isEqualTo(10)
        assertThat(result.nextPeriod).isNotNull()
        assertThat(result.nextPeriod!!.earliest.isBefore(result.nextPeriod!!.latest)).isTrue()
        assertThat(result.fertileWindow).isNotNull()
        assertThat(result.estimatedOvulation).isNotNull()
    }

    @Test
    fun highConfidence_whenRegularCyclesAndLowVariability() {
        val cycles = threeRegularCycles()
        val result =
            CyclePredictionEngineV1.predict(
                CyclePredictionInput(
                    today = LocalDate.of(2025, 7, 10),
                    completedCycles = cycles,
                    openPeriodStart = LocalDate.of(2025, 7, 1),
                    openPeriodEnd = LocalDate.of(2025, 7, 5),
                    typicalCycleLengthDays = null,
                    typicalPeriodLengthDays = null,
                    regularity = CycleRegularity.REGULAR,
                ),
            )
        assertThat(result.confidence).isEqualTo(PredictionConfidence.HIGH)
    }

    @Test
    fun irregularRegularity_widensNextPeriodRange() {
        val cycles = twoCyclesDaysApart(firstStart = LocalDate.of(2025, 4, 1), secondStart = LocalDate.of(2025, 5, 1))
        val regular =
            CyclePredictionEngineV1.predict(
                baseInput(cycles, CycleRegularity.REGULAR, today = LocalDate.of(2025, 6, 10)),
            )
        val irregular =
            CyclePredictionEngineV1.predict(
                baseInput(cycles, CycleRegularity.IRREGULAR, today = LocalDate.of(2025, 6, 10)),
            )
        val regularSpan = regular.nextPeriod!!.latest.toEpochDay() - regular.nextPeriod!!.earliest.toEpochDay()
        val irregularSpan = irregular.nextPeriod!!.latest.toEpochDay() - irregular.nextPeriod!!.earliest.toEpochDay()
        assertThat(irregularSpan).isGreaterThan(regularSpan)
    }

    @Test
    fun clampsExtremeTypicalCycleLengthToFortyFiveDays() {
        val result =
            CyclePredictionEngineV1.predict(
                CyclePredictionInput(
                    today = LocalDate.of(2025, 3, 20),
                    completedCycles = emptyList(),
                    openPeriodStart = LocalDate.of(2025, 3, 1),
                    openPeriodEnd = null,
                    typicalCycleLengthDays = 90,
                    typicalPeriodLengthDays = 5,
                    regularity = CycleRegularity.SOMEWHAT_IRREGULAR,
                ),
            )
        assertThat(result.nextPeriod).isNotNull()
        val expectedStart = LocalDate.of(2025, 3, 1).plusDays(45)
        assertThat(result.nextPeriod!!.earliest).isAtMost(expectedStart)
        assertThat(result.nextPeriod!!.latest).isAtLeast(expectedStart)
    }

    @Test
    fun ovulationEstimate_usesFourteenDayLutealPhaseBeforeNextPeriod() {
        val cycles = twoCyclesDaysApart(firstStart = LocalDate.of(2025, 4, 1), secondStart = LocalDate.of(2025, 5, 1))
        val result =
            CyclePredictionEngineV1.predict(
                baseInput(cycles, CycleRegularity.REGULAR, today = LocalDate.of(2025, 5, 15)),
            )
        val nextPeriodStart = LocalDate.of(2025, 5, 1).plusDays(30)
        val ovulationCenter = nextPeriodStart.minusDays(14)
        assertThat(result.estimatedOvulation!!.earliest).isAtMost(ovulationCenter)
        assertThat(result.estimatedOvulation!!.latest).isAtLeast(ovulationCenter)
    }

    @Test
    fun lowConfidence_whenOnlyTypicalLengthsProvided() {
        val result =
            CyclePredictionEngineV1.predict(
                CyclePredictionInput(
                    today = LocalDate.of(2025, 3, 12),
                    completedCycles = emptyList(),
                    openPeriodStart = LocalDate.of(2025, 3, 1),
                    openPeriodEnd = null,
                    typicalCycleLengthDays = 30,
                    typicalPeriodLengthDays = 4,
                    regularity = CycleRegularity.SOMEWHAT_IRREGULAR,
                ),
            )
        assertThat(result.confidence).isEqualTo(PredictionConfidence.LOW)
        assertThat(result.nextPeriod).isNotNull()
    }

    private fun baseInput(
        cycles: List<CompletedCycleSpan>,
        regularity: CycleRegularity,
        today: LocalDate,
    ): CyclePredictionInput =
        CyclePredictionInput(
            today = today,
            completedCycles = cycles,
            openPeriodStart = cycles.last().periodStart,
            openPeriodEnd = cycles.last().periodEnd,
            typicalCycleLengthDays = null,
            typicalPeriodLengthDays = null,
            regularity = regularity,
        )

    private fun twoCyclesDaysApart(
        firstStart: LocalDate,
        secondStart: LocalDate,
    ): List<CompletedCycleSpan> =
        listOf(
            CompletedCycleSpan(
                periodStart = firstStart,
                periodEnd = firstStart.plusDays(4),
                nextPeriodStart = secondStart,
            ),
            CompletedCycleSpan(
                periodStart = secondStart,
                periodEnd = secondStart.plusDays(4),
                nextPeriodStart = null,
            ),
        )

    private fun threeRegularCycles(): List<CompletedCycleSpan> =
        listOf(
            CompletedCycleSpan(
                periodStart = LocalDate.of(2025, 3, 1),
                periodEnd = LocalDate.of(2025, 3, 5),
                nextPeriodStart = LocalDate.of(2025, 4, 1),
            ),
            CompletedCycleSpan(
                periodStart = LocalDate.of(2025, 4, 1),
                periodEnd = LocalDate.of(2025, 4, 5),
                nextPeriodStart = LocalDate.of(2025, 5, 1),
            ),
            CompletedCycleSpan(
                periodStart = LocalDate.of(2025, 5, 1),
                periodEnd = LocalDate.of(2025, 5, 5),
                nextPeriodStart = LocalDate.of(2025, 6, 1),
            ),
            CompletedCycleSpan(
                periodStart = LocalDate.of(2025, 6, 1),
                periodEnd = LocalDate.of(2025, 6, 5),
                nextPeriodStart = LocalDate.of(2025, 7, 1),
            ),
        )
}
