package dev.mahin.core.database

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test

class SqliteFileProbeTest {
    @Test
    fun detectsPlaintextSqliteHeader() {
        val file = File.createTempFile("probe", ".db")
        file.writeBytes("SQLite format 3\u0000".toByteArray(Charsets.US_ASCII) + ByteArray(8))
        assertThat(SqliteFileProbe.isPlaintextSqliteDatabase(file)).isTrue()
        file.delete()
    }

    @Test
    fun rejectsNonSqliteBytes() {
        val file = File.createTempFile("probe", ".db")
        file.writeBytes(byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07))
        assertThat(SqliteFileProbe.isPlaintextSqliteDatabase(file)).isFalse()
        file.delete()
    }
}
