package dev.mahin.core.database

import android.content.Context
import androidx.room.Room
import dev.mahin.core.security.DeviceKeyMaterial
import java.io.File
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

object MahinDatabaseFactory {
    const val DATABASE_NAME: String = "mahin.db"

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
        maybeRecreateForEncryption(appContext, encryptionMode)
        val builder =
            Room
                .databaseBuilder(appContext, MahinDatabase::class.java, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2)
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
        return builder.build()
    }

    /**
     * M0 shipped an unencrypted v1 file with only app_meta. M2 enables SQLCipher before
     * health rows exist by deleting the bootstrap file (app_meta is non-sensitive).
     */
    private fun maybeRecreateForEncryption(
        context: Context,
        encryptionMode: DatabaseEncryptionMode,
    ) {
        if (encryptionMode != DatabaseEncryptionMode.KEYSTORE_SQLCIPHER) return
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        if (!dbFile.exists()) return
        val journal = File(dbFile.parent, "$DATABASE_NAME-journal")
        val wal = File(dbFile.parent, "$DATABASE_NAME-wal")
        val shm = File(dbFile.parent, "$DATABASE_NAME-shm")
        dbFile.delete()
        journal.delete()
        wal.delete()
        shm.delete()
    }
}
