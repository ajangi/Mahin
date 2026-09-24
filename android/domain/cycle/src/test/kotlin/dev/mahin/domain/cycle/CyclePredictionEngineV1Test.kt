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
}
