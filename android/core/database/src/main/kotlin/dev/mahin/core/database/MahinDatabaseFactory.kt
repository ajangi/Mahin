package dev.mahin.core.database

import android.content.Context
import androidx.room.Room
import dev.mahin.core.security.DeviceKeyMaterial
import java.io.File
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

object MahinDatabaseFactory {
    const val DATABASE_NAME: String = "mahin.db"

    private const val BOOTSTRAP_PREFS: String = "mahin_db_bootstrap"
    private const val KEY_SQLCIPHER_MIGRATED: String = "sqlcipher_bootstrap_migrated"

    private var sqlCipherLoaded: Boolean = false

    private fun loadSqlCipherNativeLibrary() {
        if (sqlCipherLoaded) return
        System.loadLibrary("sqlcipher")
        sqlCipherLoaded = true
    }

    fun build(
        context: Context,
        keyMaterial: DeviceKeyMaterial,
        encryptionMode: DatabaseEncryptionMode,
    ): MahinDatabase {
        val appContext = context.applicationContext
        maybeDropPlaintextBootstrapOnly(appContext, encryptionMode)
        val builder =
            Room
                .databaseBuilder(appContext, MahinDatabase::class.java, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        when (encryptionMode) {
            DatabaseEncryptionMode.KEYSTORE_SQLCIPHER -> {
                loadSqlCipherNativeLibrary()
                val factory = SupportOpenHelperFactory(keyMaterial.databasePassphrase())
                builder.openHelperFactory(factory)
            }
            DatabaseEncryptionMode.UNENCRYPTED_BOOTSTRAP -> {
                // M0 bootstrap only; health tables must not be written in this mode.
            }
        }
        val database = builder.build()
        if (encryptionMode == DatabaseEncryptionMode.KEYSTORE_SQLCIPHER) {
            database.openHelper.writableDatabase.close()
            markSqlCipherBootstrapComplete(appContext)
        }
        return database
    }

    /**
     * One-time: remove M0 plaintext `app_meta`-only file before first SQLCipher open.
     * Never deletes an existing encrypted database or after [KEY_SQLCIPHER_MIGRATED] is set.
     */
    internal fun maybeDropPlaintextBootstrapOnly(
        context: Context,
        encryptionMode: DatabaseEncryptionMode,
    ) {
        if (encryptionMode != DatabaseEncryptionMode.KEYSTORE_SQLCIPHER) return
        val prefs = context.getSharedPreferences(BOOTSTRAP_PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SQLCIPHER_MIGRATED, false)) return

        val dbFile = context.getDatabasePath(DATABASE_NAME)
        if (!dbFile.exists()) return
        if (!SqliteFileProbe.isPlaintextSqliteDatabase(dbFile)) return

        deleteDatabaseFiles(dbFile)
    }

    internal fun markSqlCipherBootstrapComplete(context: Context) {
        context
            .getSharedPreferences(BOOTSTRAP_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SQLCIPHER_MIGRATED, true)
            .commit()
    }

    private fun deleteDatabaseFiles(dbFile: File) {
        val parent = dbFile.parentFile
        val baseName = dbFile.name
        dbFile.delete()
        parent?.let { dir ->
            File(dir, "$baseName-journal").delete()
            File(dir, "$baseName-wal").delete()
            File(dir, "$baseName-shm").delete()
        }
    }
}
