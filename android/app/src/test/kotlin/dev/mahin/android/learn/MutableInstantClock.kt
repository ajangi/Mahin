package dev.mahin.android.learn

import dev.mahin.core.common.InstantClock
import java.time.Instant

class MutableInstantClock(
    var epochMillis: Long = 0L,
) : InstantClock {
    override fun now(): Instant = Instant.ofEpochMilli(epochMillis)
}
