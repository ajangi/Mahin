package dev.mahin.core.database

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.nio.charset.StandardCharsets
import org.junit.Test

/**
 * Validates local encryption prerequisites without loading SQLCipher JNI on JVM CI.
 */
class LocalEncryptionValidationTest {
    @Test
    fun plaintextProbeDetectsBootstrapHeaderOnly() {
        val temp = File.createTempFile("mahin-bootstrap", ".db")
        temp.writeBytes(SQLITE_HEADER)
        try {
            assertThat(SqliteFileProbe.isPlaintextSqliteDatabase(temp)).isTrue()
        } finally {
            temp.delete()
        }
    }

    @Test
    fun nonSqlitePayloadIsNotPlaintext() {
        val temp = File.createTempFile("mahin-encrypted", ".db")
        temp.writeBytes("not-sqlite-data".toByteArray(StandardCharsets.UTF_8))
        try {
            assertThat(SqliteFileProbe.isPlaintextSqliteDatabase(temp)).isFalse()
        } finally {
            temp.delete()
        }
    }

    companion object {
        private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(StandardCharsets.UTF_8)
    }
}
