package dev.mahin.core.database

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Regression for gatekeeper #1: bootstrap drop is one-shot and plaintext-only.
 * Full SQLCipher round-trip is covered on device; JVM Robolectric cannot load SQLCipher JNI on Linux CI.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MahinDatabaseBootstrapDropTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Before
    @After
    fun reset() {
        context.deleteDatabase(MahinDatabaseFactory.DATABASE_NAME)
        context
            .getSharedPreferences("mahin_db_bootstrap", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun dropsOnlyPlaintextSqliteBootstrap() {
        val dbFile = context.getDatabasePath(MahinDatabaseFactory.DATABASE_NAME)
        dbFile.parentFile?.mkdirs()
        dbFile.writeBytes(
            "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII) + ByteArray(32),
        )
        MahinDatabaseFactory.maybeDropPlaintextBootstrapOnly(
            context,
            DatabaseEncryptionMode.KEYSTORE_SQLCIPHER,
        )
        assertThat(dbFile.exists()).isFalse()
    }

    @Test
    fun doesNotDeleteNonPlaintextFile_onRepeatedColdStartChecks() {
        val dbFile = context.getDatabasePath(MahinDatabaseFactory.DATABASE_NAME)
        dbFile.parentFile?.mkdirs()
        val encryptedLikePayload = ByteArray(64) { index -> (index * 7 and 0xff).toByte() }
        dbFile.writeBytes(encryptedLikePayload)

        repeat(3) {
            MahinDatabaseFactory.maybeDropPlaintextBootstrapOnly(
                context,
                DatabaseEncryptionMode.KEYSTORE_SQLCIPHER,
            )
        }
        assertThat(dbFile.exists()).isTrue()
        assertThat(SqliteFileProbe.isPlaintextSqliteDatabase(dbFile)).isFalse()
    }

    @Test
    fun migratedFlag_preventsPlaintextDrop() {
        val dbFile = context.getDatabasePath(MahinDatabaseFactory.DATABASE_NAME)
        dbFile.parentFile?.mkdirs()
        dbFile.writeBytes(
            "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII) + ByteArray(32),
        )
        MahinDatabaseFactory.markSqlCipherBootstrapComplete(context)
        MahinDatabaseFactory.maybeDropPlaintextBootstrapOnly(
            context,
            DatabaseEncryptionMode.KEYSTORE_SQLCIPHER,
        )
        assertThat(dbFile.exists()).isTrue()
    }
}
