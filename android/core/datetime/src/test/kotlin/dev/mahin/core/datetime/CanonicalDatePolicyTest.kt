package dev.mahin.core.datetime

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class CanonicalDatePolicyTest {
    @Test
    fun persistedLocalDateIsIsoGregorian() {
        val date = LocalDate.of(2026, 3, 20)
        assertThat(CanonicalDatePolicy.requirePersistedLocalDate(date)).isEqualTo("2026-03-20")
        assertThat(CanonicalDatePolicy.parsePersistedLocalDate("2026-03-20")).isEqualTo(date)
    }

    @Test(expected = IllegalArgumentException::class)
    fun jalaliDateRejectsInvalidMonth() {
        JalaliDate(year = 1403, month = 13, day = 1)
    }
}
