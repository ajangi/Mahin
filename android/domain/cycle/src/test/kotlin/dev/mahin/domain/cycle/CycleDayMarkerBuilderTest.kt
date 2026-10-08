package dev.mahin.domain.cycle

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class CycleDayMarkerBuilderTest {
    private val anchor = LocalDate.of(2025, 3, 1)
    private val today = LocalDate.of(2025, 3, 20)

    @Test
    fun openPeriod_endInclusiveIsToday() {
        val flags =
            CycleDayMarkerBuilder.forDate(
                CycleDayMarkerInput(
                    date = today,
                    today = today,
                    periodSpans = listOf(PeriodSpanMarker(anchor, null)),
                    prediction = emptyPrediction(),
                    datesWithLogEntries = emptySet(),
                ),
            )
        assertThat(flags.loggedPeriod).isTrue()
    }

    @Test
    fun predictedPeriod_notWhenLogged() {
        val flags =
            CycleDayMarkerBuilder.forDate(
                CycleDayMarkerInput(
                    date = anchor,
                    today = today,
                    periodSpans = listOf(PeriodSpanMarker(anchor, anchor.plusDays(4))),
                    prediction =
                        emptyPrediction().copy(
                            nextPeriod = DateRangeEstimate(anchor, anchor.plusDays(2)),
                        ),
                    datesWithLogEntries = emptySet(),
                ),
            )
        assertThat(flags.loggedPeriod).isTrue()
        assertThat(flags.predictedPeriod).isFalse()
    }

    @Test
    fun fertileAndOvulation_flagsSetFromPrediction() {
        val fertileDay = anchor.plusDays(12)
        val flags =
            CycleDayMarkerBuilder.forDate(
                CycleDayMarkerInput(
                    date = fertileDay,
                    today = today,
                    periodSpans = emptyList(),
                    prediction =
                        emptyPrediction().copy(
                            fertileWindow = DateRangeEstimate(fertileDay, fertileDay),
                            estimatedOvulation = DateRangeEstimate(fertileDay, fertileDay),
                        ),
                    datesWithLogEntries = emptySet(),
                ),
            )
        assertThat(flags.fertileWindow).isTrue()
        assertThat(flags.estimatedOvulation).isTrue()
    }

    @Test
    fun buildMap_coversRange() {
        val map =
            CycleDayMarkerBuilder.buildMap(
                rangeStart = anchor,
                rangeEnd = anchor.plusDays(2),
                today = today,
                periodSpans = listOf(PeriodSpanMarker(anchor, anchor.plusDays(1))),
                prediction = emptyPrediction(),
                datesWithLogEntries = setOf(anchor.plusDays(1)),
            )
        assertThat(map).hasSize(3)
        assertThat(map[anchor.plusDays(1)]?.hasLogEntries).isTrue()
    }

    private fun emptyPrediction(): CyclePredictionResult =
        CyclePredictionResult(
            algorithmVersion = CycleDomainModule.PREDICTION_ALGORITHM_VERSION,
            confidence = PredictionConfidence.MEDIUM,
            cycleDay = 5,
            nextPeriod = null,
            fertileWindow = null,
            estimatedOvulation = null,
            insufficientDataReason = null,
        )
}
