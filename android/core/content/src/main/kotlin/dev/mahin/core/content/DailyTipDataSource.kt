package dev.mahin.core.content

interface DailyTipDataSource {
    suspend fun fetchDailyTips(key: DailyTipTargetingKey): DailyTipFetchResult

    suspend fun cachedDailyTipFor(key: DailyTipTargetingKey): ContentDailyTipDto?

    suspend fun cacheDailyTip(
        tip: ContentDailyTipDto,
        key: DailyTipTargetingKey,
    )

    suspend fun clearCachedDailyTip()
}
