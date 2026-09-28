package dev.mahin.core.security

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PinHasherTest {
    @Test
    fun verifyPinMatchesStoredHash() {
        val salt = PinHasher.generateSalt()
        val hash = PinHasher.hashPin("1234", salt)
        assertThat(PinHasher.verifyPin("1234", salt, hash)).isTrue()
        assertThat(PinHasher.verifyPin("9999", salt, hash)).isFalse()
    }
}
