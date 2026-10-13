package dev.mahin.core.content

interface DailyTipDataSource {
    suspend fun dailyTips(
        mode: String?,
        cyclePhase: String?,
        pregnancyWeek: Int?,
    ): List<ContentDailyTipDto>

    suspend fun cachedDailyTip(): ContentDailyTipDto?

    suspend fun cacheDailyTip(tip: ContentDailyTipDto)
}
