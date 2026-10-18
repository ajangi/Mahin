package dev.mahin.android.cycle.editor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.robolectric.shadows.ShadowLooper

internal suspend fun awaitDailyLogOnMain(
    timeoutMs: Long = 5_000,
    condition: () -> Boolean,
) {
    withTimeout(timeoutMs) {
        while (true) {
            if (condition()) return@withTimeout
            val main = Dispatchers.Main
            if (main is TestDispatcher) {
                main.scheduler.advanceUntilIdle()
            }
            ShadowLooper.idleMainLooper()
            yield()
        }
    }
}
