package dev.mahin.android.learn

import kotlinx.coroutines.delay

interface PlayerSessionTicker {
    suspend fun waitNextTick()
}

object DefaultPlayerSessionTicker : PlayerSessionTicker {
    override suspend fun waitNextTick() {
        delay(250)
    }
}
