package dev.mahin.domain.pregnancy

import dev.mahin.core.model.PregnancyDatingSource

data class PregnancyTodayHero(
    val gestationalWeeks: Int,
    val gestationalDays: Int,
    val trimester: PregnancyTrimester,
    val daysUntilEdd: Long,
    val datingSource: PregnancyDatingSource,
    val displayWeekNumber: Int,
    val totalGestationalDays: Int = 280,
)

data class PregnancyTodaySnapshot(
    val hero: PregnancyTodayHero,
)

object PregnancyTodaySnapshotUseCase {
    fun fromStatus(status: PregnancyStatusSnapshot): PregnancyTodaySnapshot {
        val ga = status.gestationalAge
        return PregnancyTodaySnapshot(
            hero =
                PregnancyTodayHero(
                    gestationalWeeks = ga.weeks,
                    gestationalDays = ga.days,
                    trimester = status.trimester,
                    daysUntilEdd = status.daysUntilEdd,
                    datingSource = status.dating.datingSource,
                    displayWeekNumber = status.displayWeekNumber,
                ),
        )
    }
}
