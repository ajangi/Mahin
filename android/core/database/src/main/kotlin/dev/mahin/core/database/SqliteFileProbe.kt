package dev.mahin.core.database

import java.io.File

/**
 * Detects a standard (unencrypted) SQLite file header. M0 bootstrap used plaintext Room v1.
 * SQLCipher-encrypted files must not match this probe.
 */
internal object SqliteFileProbe {
    private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)

    fun isPlaintextSqliteDatabase(file: File): Boolean {
        if (!file.isFile || file.length() < SQLITE_HEADER.size) return false
        return file.inputStream().use { input ->
            val header = ByteArray(SQLITE_HEADER.size)
            val read = input.read(header)
            read == SQLITE_HEADER.size && header.contentEquals(SQLITE_HEADER)
        }
    }
}
