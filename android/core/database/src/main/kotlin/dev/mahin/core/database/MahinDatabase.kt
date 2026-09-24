package dev.mahin.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AppMetaEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class MahinDatabase : RoomDatabase() {
    abstract fun appMetaDao(): AppMetaDao
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
