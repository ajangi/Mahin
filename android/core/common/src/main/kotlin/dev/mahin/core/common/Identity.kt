package dev.mahin.core.common

import java.time.Clock
import java.time.Instant
import java.util.UUID

fun interface UuidFactory {
    fun random(): UUID
}

object RandomUuidFactory : UuidFactory {
    override fun random(): UUID = UUID.randomUUID()
}

fun interface InstantClock {
    fun now(): Instant
}

class SystemInstantClock(
    private val clock: Clock = Clock.systemUTC(),
) : InstantClock {
    override fun now(): Instant = Instant.now(clock)
}

class FixedInstantClock(
    private var fixed: Instant,
) : InstantClock {
    override fun now(): Instant = fixed

    fun set(instant: Instant) {
        fixed = instant
    }

    fun advanceMillis(millis: Long) {
        fixed = fixed.plusMillis(millis)
    }
}
