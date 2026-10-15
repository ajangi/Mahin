package dev.mahin.android.cycle.editor

import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.robolectric.shadows.ShadowLooper

internal suspend fun awaitDailyLogOnMain(
    timeoutMs: Long = 5_000,
    condition: () -> Boolean,
) {
    withTimeout(timeoutMs) {
        while (true) {
            ShadowLooper.idleMainLooper()
            if (condition()) return@withTimeout
            yield()
        }
    }
}
