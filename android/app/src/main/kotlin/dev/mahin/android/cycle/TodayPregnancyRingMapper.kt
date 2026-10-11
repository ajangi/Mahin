package dev.mahin.android.cycle

import androidx.compose.runtime.Composable
import dev.mahin.core.designsystem.LocalMahinExtendedColors
import dev.mahin.core.designsystem.component.MahinRingArc
import dev.mahin.domain.pregnancy.PregnancyTodayHero

internal object TodayPregnancyRingMapper {
    private const val WEEKS_TOTAL = 40f

    @Composable
    fun weekArcs(hero: PregnancyTodayHero): List<MahinRingArc> {
        val extended = LocalMahinExtendedColors.current
        val completedWeeks = hero.gestationalWeeks.coerceIn(0, 40)
        if (completedWeeks <= 0) return emptyList()
        return listOf(
            MahinRingArc(
                color = extended.healthPregnancy,
                startFraction = 0f,
                endFraction = completedWeeks / WEEKS_TOTAL,
            ),
        )
    }
}
