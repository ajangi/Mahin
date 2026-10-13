package dev.mahin.android.cycle.editor

import org.robolectric.shadows.ShadowLooper

internal suspend fun awaitDailyLogOnMain(
    timeoutMs: Long = 5_000,
    condition: () -> Boolean,
) {
    val deadline = System.currentTimeMillis() + timeoutMs
    while (System.currentTimeMillis() < deadline) {
        ShadowLooper.idleMainLooper()
        if (condition()) return
    }
    throw AssertionError("Condition not met within ${timeoutMs}ms")
}
