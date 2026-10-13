package dev.mahin.android.learn

import kotlinx.coroutines.channels.Channel

class ManualPlayerSessionTicker : PlayerSessionTicker {
    private val ticks = Channel<Unit>(Channel.UNLIMITED)

    override suspend fun waitNextTick() {
        ticks.receive()
    }

    fun advanceOneTick() {
        ticks.trySend(Unit)
    }
}
