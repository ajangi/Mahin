package dev.mahin.android.cycle

import dev.mahin.core.database.entity.DailyLogEntity

internal object TodayLoggedSummaryMapper {
    fun fromEntity(entity: DailyLogEntity): TodayLoggedSummary {
        val chips = mutableListOf<TodayLoggedChip>()
        if (entity.symptomTags.isNotBlank()) {
            entity.symptomTags.split(',').map { it.trim() }.filter { it.isNotEmpty() }.take(3).forEach { tag ->
                chips +=
                    TodayLoggedChip(
                        icon = MahinLogTagIconMapper.symptomIcon(tag),
                        label = tag,
                    )
            }
        }
        if (entity.moodTags.isNotBlank()) {
            entity.moodTags.split(',').map { it.trim() }.filter { it.isNotEmpty() }.take(2).forEach { tag ->
                chips +=
                    TodayLoggedChip(
                        icon = MahinLogTagIconMapper.moodIcon(tag),
                        label = tag,
                    )
            }
        }
        return TodayLoggedSummary(chips = chips)
    }
}
