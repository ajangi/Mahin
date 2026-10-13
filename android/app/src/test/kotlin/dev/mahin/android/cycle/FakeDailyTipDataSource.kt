package dev.mahin.android.cycle

import dev.mahin.core.content.ContentDailyTipDto
import dev.mahin.core.content.DailyTipDataSource
import dev.mahin.core.content.DailyTipFetchResult
import dev.mahin.core.content.DailyTipTargetingKey

class FakeDailyTipDataSource : DailyTipDataSource {
    var tips: List<ContentDailyTipDto> = emptyList()
    var throwOnFetch: Boolean = false
    var fetchCount: Int = 0
    var lastFetchKey: DailyTipTargetingKey? = null

    private var cachedTip: ContentDailyTipDto? = null
    private var cachedKey: DailyTipTargetingKey? = null

    override suspend fun fetchDailyTips(key: DailyTipTargetingKey): DailyTipFetchResult {
        fetchCount += 1
        lastFetchKey = key
        if (throwOnFetch) {
            return DailyTipFetchResult.NetworkFailure(IllegalStateException("offline"))
        }
        return DailyTipFetchResult.Success(tips)
    }

    override suspend fun cachedDailyTipFor(key: DailyTipTargetingKey): ContentDailyTipDto? {
        val envelopeKey = cachedKey
        return if (envelopeKey == key) cachedTip else null
    }

    override suspend fun cacheDailyTip(
        tip: ContentDailyTipDto,
        key: DailyTipTargetingKey,
    ) {
        cachedTip = tip
        cachedKey = key
    }

    override suspend fun clearCachedDailyTip() {
        cachedTip = null
        cachedKey = null
    }

    fun seedCache(
        tip: ContentDailyTipDto,
        key: DailyTipTargetingKey,
    ) {
        cachedTip = tip
        cachedKey = key
    }
}
