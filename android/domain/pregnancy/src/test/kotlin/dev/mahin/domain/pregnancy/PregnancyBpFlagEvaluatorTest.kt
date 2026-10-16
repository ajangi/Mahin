package dev.mahin.domain.pregnancy

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PregnancyBpFlagEvaluatorTest {
    private val thresholds =
        PregnancyBpThresholds(
            elevatedSystolic = 140,
            elevatedDiastolic = 90,
            urgentSystolic = 160,
            urgentDiastolic = 110,
        )

    @Test
    fun evaluate_noneWhenMissingInputs() {
        assertThat(PregnancyBpFlagEvaluator.evaluate(null, thresholds)).isEqualTo(PregnancyBpFlagLevel.NONE)
        assertThat(
            PregnancyBpFlagEvaluator.evaluate(PregnancyBpReading(120, 80), null),
        ).isEqualTo(PregnancyBpFlagLevel.NONE)
    }

    @Test
    fun evaluate_elevatedAndUrgentFromThresholds() {
        assertThat(
            PregnancyBpFlagEvaluator.evaluate(PregnancyBpReading(145, 85), thresholds),
        ).isEqualTo(PregnancyBpFlagLevel.ELEVATED)
        assertThat(
            PregnancyBpFlagEvaluator.evaluate(PregnancyBpReading(120, 95), thresholds),
        ).isEqualTo(PregnancyBpFlagLevel.ELEVATED)
        assertThat(
            PregnancyBpFlagEvaluator.evaluate(PregnancyBpReading(165, 85), thresholds),
        ).isEqualTo(PregnancyBpFlagLevel.URGENT)
    }
}
