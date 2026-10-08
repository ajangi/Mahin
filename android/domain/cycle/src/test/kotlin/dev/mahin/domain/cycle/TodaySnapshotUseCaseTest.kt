package dev.mahin.domain.cycle

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
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
                    typicalPeriodLengthDays = 5,
                    onPeriodToday = false,
                ),
            ) as TodaySnapshot.Cycle
        assertThat(snapshot.hero.cycleDay).isEqualTo(14)
        assertThat(snapshot.hero.isInFertileWindow).isTrue()
        assertThat(snapshot.hero.isOverdue).isFalse()
        assertThat(
            snapshot.hero.ringSegments.any { it.kind == CycleRingSegmentKind.FERTILE_WINDOW },
        ).isTrue()
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
                    typicalPeriodLengthDays = 5,
                    onPeriodToday = false,
                ),
            ) as TodaySnapshot.Cycle
        assertThat(snapshot.hero.isOverdue).isTrue()
        assertThat(snapshot.hero.daysUntilNextPeriodEarliest).isLessThan(0)
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
                    typicalPeriodLengthDays = 5,
                    onPeriodToday = false,
                ),
            ) as TodaySnapshot.Cycle
        assertThat(snapshot.hero.confidence).isEqualTo(PredictionConfidence.LOW)
        assertThat(snapshot.hero.showConfidenceChip).isTrue()
    }
}
