package dev.mahin.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AppMetaEntity::class],
    version = MahinDatabase.VERSION,
    exportSchema = true,
)
abstract class MahinDatabase : RoomDatabase() {
    abstract fun appMetaDao(): AppMetaDao

    companion object {
        const val VERSION: Int = 1
        const val EXPORTS_SCHEMA: Boolean = true
    }
}

interface MahinDatabaseProvider {
    fun database(): MahinDatabase
}

/**
 * Encryption is required before health rows exist (see ADR 0007). M0 stores only
 * non-sensitive app_meta so the Room pipeline is real, not a throwaway stub.
 */
enum class DatabaseEncryptionMode {
    UNENCRYPTED_BOOTSTRAP,
    KEYSTORE_SQLCIPHER,
}
