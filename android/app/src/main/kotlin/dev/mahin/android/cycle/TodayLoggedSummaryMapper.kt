package dev.mahin.android.cycle

import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.designsystem.icon.MahinIcons

internal object TodayLoggedSummaryMapper {
    fun fromEntity(entity: DailyLogEntity): TodayLoggedSummary {
        val chips = mutableListOf<TodayLoggedChip>()
        if (entity.symptomTags.isNotBlank()) {
            entity.symptomTags.split(',').map { it.trim() }.filter { it.isNotEmpty() }.take(3).forEach {
                chips +=
                    TodayLoggedChip(
                        icon = MahinIcons.Symptom.fatigue,
                        label = it,
                    )
            }
        }
        if (entity.moodTags.isNotBlank()) {
            entity.moodTags.split(',').map { it.trim() }.filter { it.isNotEmpty() }.take(2).forEach {
                chips +=
                    TodayLoggedChip(
                        icon = MahinIcons.Mood.calm,
                        label = it,
                    )
            }
        }
        return TodayLoggedSummary(chips = chips)
    }
}
