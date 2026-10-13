package dev.mahin.android.cycle

import dev.mahin.core.content.ContentDailyTipDto
import dev.mahin.core.content.DailyTipDataSource

class FakeDailyTipDataSource : DailyTipDataSource {
    var tips: List<ContentDailyTipDto> = emptyList()
    var throwOnFetch: Boolean = false
    var cached: ContentDailyTipDto? = null

    override suspend fun dailyTips(
        mode: String?,
        cyclePhase: String?,
        pregnancyWeek: Int?,
    ): List<ContentDailyTipDto> {
        if (throwOnFetch) error("offline")
        return tips
    }

    override suspend fun cachedDailyTip(): ContentDailyTipDto? = cached

    override suspend fun cacheDailyTip(tip: ContentDailyTipDto) {
        cached = tip
    }
}
