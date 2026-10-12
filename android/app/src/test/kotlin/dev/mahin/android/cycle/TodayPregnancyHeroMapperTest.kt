package dev.mahin.android.cycle

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.PregnancyDatingSource
import dev.mahin.domain.pregnancy.PregnancyTodayHero
import dev.mahin.domain.pregnancy.PregnancyTrimester
import org.junit.Test

class TodayPregnancyHeroMapperTest {
    @Test
    fun todayMarkerFraction_day0_isNearStart() {
        val hero =
            PregnancyTodayHero(
                gestationalWeeks = 0,
                gestationalDays = 0,
                trimester = PregnancyTrimester.FIRST,
                daysUntilEdd = 280,
                datingSource = PregnancyDatingSource.LMP_PLUS_280_DAYS,
                displayWeekNumber = 1,
            )
        assertThat(TodayPregnancyHeroMapper.todayMarkerFraction(hero)).isWithin(0.001f).of(0.5f / 280f)
    }

    @Test
    fun todayMarkerFraction_middleDay_isNearHalf() {
        val hero =
            PregnancyTodayHero(
                gestationalWeeks = 20,
                gestationalDays = 0,
                trimester = PregnancyTrimester.SECOND,
                daysUntilEdd = 140,
                datingSource = PregnancyDatingSource.LMP_PLUS_280_DAYS,
                displayWeekNumber = 21,
            )
        val day = 20 * 7
        assertThat(TodayPregnancyHeroMapper.todayMarkerFraction(hero))
            .isWithin(0.001f)
            .of((day + 0.5f) / 280f)
    }

    @Test
    fun todayMarkerFraction_lastDay_isNearEnd() {
        val hero =
            PregnancyTodayHero(
                gestationalWeeks = 39,
                gestationalDays = 6,
                trimester = PregnancyTrimester.THIRD,
                daysUntilEdd = 0,
                datingSource = PregnancyDatingSource.LMP_PLUS_280_DAYS,
                displayWeekNumber = 40,
            )
        val day = 39 * 7 + 6
        assertThat(TodayPregnancyHeroMapper.todayMarkerFraction(hero))
            .isWithin(0.001f)
            .of((day + 0.5f) / 280f)
    }
}
