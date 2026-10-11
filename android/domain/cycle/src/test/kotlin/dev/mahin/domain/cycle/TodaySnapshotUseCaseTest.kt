package dev.mahin.domain.cycle

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import org.junit.Test

class TodaySnapshotUseCaseTest {
    private val anchor = LocalDate.of(2025, 3, 1)
    private val today = LocalDate.of(2025, 3, 14)

    @Test
    fun firstDay_whenNoAnchorAndNoCycleDay() {
        val result =
            TodaySnapshotUseCase.fromCycle(
                CycleTodaySnapshotInput(
                    today = today,
                    prediction =
                        CyclePredictionResult(
                            algorithmVersion = CycleDomainModule.PREDICTION_ALGORITHM_VERSION,
                            confidence = PredictionConfidence.INSUFFICIENT_DATA,
                            cycleDay = null,
                            nextPeriod = null,
                            fertileWindow = null,
                            estimatedOvulation = null,
                            insufficientDataReason = "no_period_anchor",
                        ),
                    periodAnchorStart = null,
                    currentPeriod = null,
                    typicalPeriodLengthDays = 5,
                    onPeriodToday = false,
                ),
            )
        assertThat(result).isEqualTo(TodaySnapshot.FirstDay)
    }

    @Test
    fun regularCycle_buildsHeroWithFertileSegments() {
        val prediction =
            CyclePredictionEngineV1.predict(
                CyclePredictionInput(
                    today = today,
                    completedCycles =
                        listOf(
                            CompletedCycleSpan(anchor.minusDays(28), anchor.minusDays(24), anchor),
                            CompletedCycleSpan(anchor.minusDays(56), anchor.minusDays(52), anchor.minusDays(28)),
                        ),
                    openPeriodStart = anchor,
                    openPeriodEnd = anchor.plusDays(4),
                    typicalCycleLengthDays = 28,
                    typicalPeriodLengthDays = 5,
                    regularity = dev.mahin.core.model.CycleRegularity.REGULAR,
                ),
            )
        val snapshot =
            TodaySnapshotUseCase.fromCycle(
                CycleTodaySnapshotInput(
                    today = today,
                    prediction = prediction,
                    periodAnchorStart = anchor,
                    currentPeriod = PeriodSpanForSnapshot(anchor, anchor.plusDays(4)),
                    typicalPeriodLengthDays = 5,
                    onPeriodToday = false,
                ),
            ) as TodaySnapshot.Cycle
        assertThat(snapshot.hero.cycleDay).isEqualTo(14)
        val logged =
            snapshot.hero.ringSegments.first { it.kind == CycleRingSegmentKind.LOGGED_PERIOD }
        assertThat(logged.startDay).isEqualTo(1)
        assertThat(logged.endDay).isEqualTo(5)
        assertThat(snapshot.hero.isInFertileWindow).isTrue()
        assertThat(snapshot.hero.isOverdue).isFalse()
        assertSegmentMatchesPrediction(
            anchor = anchor,
            prediction = prediction,
            segment =
                snapshot.hero.ringSegments.first {
                    it.kind == CycleRingSegmentKind.PREDICTED_PERIOD
                },
            range = prediction.nextPeriod!!,
        )
        assertSegmentMatchesPrediction(
            anchor = anchor,
            prediction = prediction,
            segment =
                snapshot.hero.ringSegments.first {
                    it.kind == CycleRingSegmentKind.FERTILE_WINDOW
                },
            range = prediction.fertileWindow!!,
        )
        assertSegmentMatchesPrediction(
            anchor = anchor,
            prediction = prediction,
            segment =
                snapshot.hero.ringSegments.first {
                    it.kind == CycleRingSegmentKind.ESTIMATED_OVULATION
                },
            range = prediction.estimatedOvulation!!,
        )
        assertThat(snapshot.hero.cycleLengthDays)
            .isEqualTo(
                cycleDayFor(anchor, prediction.nextPeriod!!.latest),
            )
    }

    @Test
    fun insufficientData_showsConfidenceChip() {
        val prediction =
            CyclePredictionResult(
                algorithmVersion = CycleDomainModule.PREDICTION_ALGORITHM_VERSION,
                confidence = PredictionConfidence.INSUFFICIENT_DATA,
                cycleDay = 3,
                nextPeriod = null,
                fertileWindow = null,
                estimatedOvulation = null,
                insufficientDataReason = "need_more_completed_cycles",
            )
        val snapshot =
            TodaySnapshotUseCase.fromCycle(
                CycleTodaySnapshotInput(
                    today = today,
                    prediction = prediction,
                    periodAnchorStart = anchor,
                    currentPeriod = PeriodSpanForSnapshot(anchor, null),
                    typicalPeriodLengthDays = 5,
                    onPeriodToday = false,
                ),
            ) as TodaySnapshot.Cycle
        assertThat(snapshot.hero.showConfidenceChip).isTrue()
    }

    @Test
    fun overdue_whenPastLatestNextPeriodEstimate() {
        val lateToday = LocalDate.of(2025, 4, 10)
        val prediction =
            CyclePredictionEngineV1.predict(
                CyclePredictionInput(
                    today = lateToday,
                    completedCycles =
                        listOf(
                            CompletedCycleSpan(anchor, anchor.plusDays(4), anchor.plusDays(28)),
                        ),
                    openPeriodStart = null,
                    openPeriodEnd = null,
                    typicalCycleLengthDays = 28,
                    typicalPeriodLengthDays = 5,
                    regularity = dev.mahin.core.model.CycleRegularity.REGULAR,
                ),
            )
        val snapshot =
            TodaySnapshotUseCase.fromCycle(
                CycleTodaySnapshotInput(
                    today = lateToday,
                    prediction = prediction,
                    periodAnchorStart = anchor,
                    currentPeriod = null,
                    typicalPeriodLengthDays = 5,
                    onPeriodToday = false,
                ),
            ) as TodaySnapshot.Cycle
        assertThat(snapshot.hero.isOverdue).isTrue()
        assertThat(snapshot.hero.daysUntilNextPeriodEarliest).isLessThan(0)
        val predicted =
            snapshot.hero.ringSegments.first { it.kind == CycleRingSegmentKind.PREDICTED_PERIOD }
        assertThat(predicted.endDay)
            .isEqualTo(cycleDayFor(anchor, prediction.nextPeriod!!.latest))
        assertThat(snapshot.hero.cycleLengthDays)
            .isEqualTo(cycleDayFor(anchor, prediction.nextPeriod!!.latest))
    }

    @Test
    fun fertileWindow_setsInFertileFlagAndSegment() {
        val fertileToday = LocalDate.of(2025, 3, 12)
        val prediction =
            CyclePredictionEngineV1.predict(
                CyclePredictionInput(
                    today = fertileToday,
                    completedCycles =
                        listOf(
                            CompletedCycleSpan(anchor.minusDays(28), anchor.minusDays(24), anchor),
                            CompletedCycleSpan(anchor.minusDays(56), anchor.minusDays(52), anchor.minusDays(28)),
                        ),
                    openPeriodStart = anchor,
                    openPeriodEnd = anchor.plusDays(4),
                    typicalCycleLengthDays = 28,
                    typicalPeriodLengthDays = 5,
                    regularity = dev.mahin.core.model.CycleRegularity.REGULAR,
                ),
            )
        val snapshot =
            TodaySnapshotUseCase.fromCycle(
                CycleTodaySnapshotInput(
                    today = fertileToday,
                    prediction = prediction,
                    periodAnchorStart = anchor,
                    currentPeriod = PeriodSpanForSnapshot(anchor, anchor.plusDays(4)),
                    typicalPeriodLengthDays = 5,
                    onPeriodToday = false,
                ),
            ) as TodaySnapshot.Cycle
        assertThat(snapshot.hero.isInFertileWindow).isTrue()
        assertSegmentMatchesPrediction(
            anchor = anchor,
            prediction = prediction,
            segment =
                snapshot.hero.ringSegments.first {
                    it.kind == CycleRingSegmentKind.FERTILE_WINDOW
                },
            range = prediction.fertileWindow!!,
        )
    }

    @Test
    fun estimatedOvulation_addsOvulationSegment() {
        val ovulationDay = LocalDate.of(2025, 3, 15)
        val prediction =
            CyclePredictionResult(
                algorithmVersion = CycleDomainModule.PREDICTION_ALGORITHM_VERSION,
                confidence = PredictionConfidence.MEDIUM,
                cycleDay = 15,
                nextPeriod = DateRangeEstimate(ovulationDay.plusDays(13), ovulationDay.plusDays(16)),
                fertileWindow = DateRangeEstimate(ovulationDay.minusDays(2), ovulationDay.plusDays(1)),
                estimatedOvulation = DateRangeEstimate(ovulationDay, ovulationDay),
                insufficientDataReason = null,
            )
        val snapshot =
            TodaySnapshotUseCase.fromCycle(
                CycleTodaySnapshotInput(
                    today = ovulationDay,
                    prediction = prediction,
                    periodAnchorStart = anchor,
                    currentPeriod = PeriodSpanForSnapshot(anchor, anchor.plusDays(4)),
                    typicalPeriodLengthDays = 5,
                    onPeriodToday = false,
                ),
            ) as TodaySnapshot.Cycle
        assertSegmentMatchesPrediction(
            anchor = anchor,
            prediction = prediction,
            segment =
                snapshot.hero.ringSegments.first {
                    it.kind == CycleRingSegmentKind.ESTIMATED_OVULATION
                },
            range = prediction.estimatedOvulation!!,
        )
        val predicted =
            snapshot.hero.ringSegments.first { it.kind == CycleRingSegmentKind.PREDICTED_PERIOD }
        assertThat(predicted.endDay)
            .isEqualTo(cycleDayFor(anchor, prediction.nextPeriod!!.latest))
    }

    @Test
    fun irregular_lowConfidence_showsChip() {
        val prediction =
            CyclePredictionEngineV1.predict(
                CyclePredictionInput(
                    today = today,
                    completedCycles =
                        listOf(
                            CompletedCycleSpan(anchor.minusDays(40), anchor.minusDays(36), anchor),
                            CompletedCycleSpan(anchor.minusDays(80), anchor.minusDays(76), anchor.minusDays(40)),
                        ),
                    openPeriodStart = anchor,
                    openPeriodEnd = null,
                    typicalCycleLengthDays = 28,
                    typicalPeriodLengthDays = 5,
                    regularity = dev.mahin.core.model.CycleRegularity.IRREGULAR,
                ),
            )
        val snapshot =
            TodaySnapshotUseCase.fromCycle(
                CycleTodaySnapshotInput(
                    today = today,
                    prediction = prediction,
                    periodAnchorStart = anchor,
                    currentPeriod = PeriodSpanForSnapshot(anchor, null),
                    typicalPeriodLengthDays = 5,
                    onPeriodToday = true,
                ),
            ) as TodaySnapshot.Cycle
        assertThat(snapshot.hero.confidence).isEqualTo(PredictionConfidence.LOW)
        assertThat(snapshot.hero.showConfidenceChip).isTrue()
    }

    private fun cycleDayFor(
        anchor: LocalDate,
        date: LocalDate,
    ): Int = (ChronoUnit.DAYS.between(anchor, date) + 1).toInt()

    private fun assertSegmentMatchesPrediction(
        anchor: LocalDate,
        prediction: CyclePredictionResult,
        segment: CycleRingSegment,
        range: DateRangeEstimate,
    ) {
        assertThat(segment.startDay).isEqualTo(cycleDayFor(anchor, range.earliest))
        assertThat(segment.endDay).isEqualTo(cycleDayFor(anchor, range.latest))
        assertThat(segment.endDay).isAtMost(
            cycleDayFor(anchor, prediction.nextPeriod!!.latest),
        )
    }
}
