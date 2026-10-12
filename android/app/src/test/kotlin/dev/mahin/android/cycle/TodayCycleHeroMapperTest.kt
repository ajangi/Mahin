package dev.mahin.android.cycle

import com.google.common.truth.Truth.assertThat
import dev.mahin.domain.cycle.CycleTodayHero
import dev.mahin.domain.cycle.PredictionConfidence
import org.junit.Test

class TodayCycleHeroMapperTest {
    @Test
    fun todayMarkerFraction_centersAtHalfDayOffset() {
        val hero =
            CycleTodayHero(
                cycleDay = 15,
                cycleLengthDays = 30,
                ringSegments = emptyList(),
                confidence = PredictionConfidence.MEDIUM,
                showConfidenceChip = false,
                daysUntilNextPeriodEarliest = 10,
                isInFertileWindow = false,
                isOverdue = false,
            )
        val fraction = TodayCycleHeroMapper.todayMarkerFraction(hero)
        assertThat(fraction).isWithin(0.001f).of((14.5f / 30f))
    }
}
