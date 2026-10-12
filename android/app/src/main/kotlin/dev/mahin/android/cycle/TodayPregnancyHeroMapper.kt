package dev.mahin.android.cycle

import dev.mahin.domain.pregnancy.PregnancyTodayHero

internal object TodayPregnancyHeroMapper {
    /**
     * Gestational day is 0-based; marker sits at the centre of the current day arc.
     */
    fun todayMarkerFraction(hero: PregnancyTodayHero): Float {
        val gestationalDay = hero.gestationalWeeks * 7 + hero.gestationalDays
        val totalDays = hero.totalGestationalDays.coerceAtLeast(1)
        return ((gestationalDay.toFloat() + 0.5f) / totalDays).coerceIn(0f, 1f)
    }
}
