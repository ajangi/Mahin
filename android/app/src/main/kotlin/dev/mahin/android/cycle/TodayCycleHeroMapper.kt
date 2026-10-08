package dev.mahin.android.cycle

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import dev.mahin.core.designsystem.LocalMahinExtendedColors
import dev.mahin.core.designsystem.component.MahinRingArc
import dev.mahin.core.designsystem.component.MahinRingArcStyle
import dev.mahin.domain.cycle.CycleRingSegmentKind
import dev.mahin.domain.cycle.CycleTodayHero

internal object TodayCycleHeroMapper {
    @Composable
    fun ringArcs(hero: CycleTodayHero): List<MahinRingArc> {
        val extended = LocalMahinExtendedColors.current
        val cycleLength = hero.cycleLengthDays.coerceAtLeast(1)
        return hero.ringSegments.map { segment ->
            val color =
                when (segment.kind) {
                    CycleRingSegmentKind.LOGGED_PERIOD -> extended.healthPeriod
                    CycleRingSegmentKind.PREDICTED_PERIOD -> extended.healthPeriod
                    CycleRingSegmentKind.FERTILE_WINDOW -> extended.healthFertility
                    CycleRingSegmentKind.ESTIMATED_OVULATION -> extended.healthOvulation
                }
            val style =
                when (segment.kind) {
                    CycleRingSegmentKind.PREDICTED_PERIOD -> MahinRingArcStyle.Dashed
                    else -> MahinRingArcStyle.Solid
                }
            MahinRingArc(
                color = color,
                startFraction = (segment.startDay - 1).toFloat() / cycleLength,
                endFraction = segment.endDay.toFloat() / cycleLength,
                style = style,
            )
        }
    }

    @Composable
    fun trackColor(): androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

    fun progressFraction(hero: CycleTodayHero): Float {
        val day = hero.cycleDay ?: return 0f
        return (day.toFloat() / hero.cycleLengthDays.coerceAtLeast(1)).coerceIn(0f, 1f)
    }
}
