package dev.mahin.core.common

import java.time.Clock
import java.time.LocalDate

fun interface LocalDateClock {
    fun today(): LocalDate
}

class SystemLocalDateClock(
    private val clock: Clock = Clock.systemDefaultZone(),
) : LocalDateClock {
    override fun today(): LocalDate = LocalDate.now(clock)
}

class FixedLocalDateClock(
    private val fixed: LocalDate,
) : LocalDateClock {
    override fun today(): LocalDate = fixed
}
