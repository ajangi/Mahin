package dev.mahin.core.content

import kotlinx.serialization.Serializable

@Serializable
data class DailyTipTargetingKey(
    val mode: String?,
    val cyclePhase: String?,
    val pregnancyWeek: Int?,
    val localDateIso: String,
)

@Serializable
data class CachedDailyTipEnvelope(
    val tip: ContentDailyTipDto,
    val key: DailyTipTargetingKey,
)

sealed interface DailyTipFetchResult {
    data class Success(
        val tips: List<ContentDailyTipDto>,
    ) : DailyTipFetchResult

    data class NetworkFailure(
        val cause: Throwable,
    ) : DailyTipFetchResult
}
