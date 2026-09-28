package dev.mahin.core.security

import java.security.MessageDigest
import java.security.SecureRandom

object PinHasher {
    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        return salt
    }

    fun hashPin(
        pin: String,
        salt: ByteArray,
    ): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        digest.update(pin.toByteArray(Charsets.UTF_8))
        return digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }

    fun verifyPin(
        pin: String,
        salt: ByteArray,
        expectedHash: String,
    ): Boolean = hashPin(pin, salt) == expectedHash

    private const val SALT_BYTES = 16
}
