package dev.mahin.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.mahin.core.database.dao.CycleProfileDao
import dev.mahin.core.database.dao.DailyLogDao
import dev.mahin.core.database.dao.PeriodDayDao
import dev.mahin.core.database.dao.PeriodRecordDao
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.database.entity.PeriodDayEntity
import dev.mahin.core.database.entity.PeriodRecordEntity

@Database(
    entities = [
        AppMetaEntity::class,
        CycleProfileEntity::class,
        PeriodRecordEntity::class,
        PeriodDayEntity::class,
        DailyLogEntity::class,
    ],
    version = MahinDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(MahinRoomConverters::class)
abstract class MahinDatabase : RoomDatabase() {
    abstract fun appMetaDao(): AppMetaDao

    abstract fun cycleProfileDao(): CycleProfileDao

    abstract fun periodRecordDao(): PeriodRecordDao

    abstract fun periodDayDao(): PeriodDayDao

    abstract fun dailyLogDao(): DailyLogDao

    companion object {
        const val VERSION: Int = 2
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
