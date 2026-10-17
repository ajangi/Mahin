package dev.mahin.android.pregnancy

internal object PregnancyHubTimerMath {
    fun elapsedSeconds(
        nowEpochMs: Long,
        startedAtEpochMs: Long?,
    ): Long =
        startedAtEpochMs?.let { start ->
            ((nowEpochMs - start) / 1000L).coerceAtLeast(0L)
        } ?: 0L
}
