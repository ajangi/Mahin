package dev.mahin.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.mahin.core.database.dao.ContractionEventDao
import dev.mahin.core.database.dao.ContractionSessionDao
import dev.mahin.core.database.dao.CycleProfileDao
import dev.mahin.core.database.dao.DailyLogDao
import dev.mahin.core.database.dao.KickEventDao
import dev.mahin.core.database.dao.KickSessionDao
import dev.mahin.core.database.dao.PeriodDayDao
import dev.mahin.core.database.dao.PeriodRecordDao
import dev.mahin.core.database.dao.PregnancyAppointmentDao
import dev.mahin.core.database.dao.PregnancyDatingRevisionDao
import dev.mahin.core.database.dao.PregnancyDayLogDao
import dev.mahin.core.database.dao.PregnancyRecordDao
import dev.mahin.core.database.dao.TtcDayLogDao
import dev.mahin.core.database.entity.ContractionEventEntity
import dev.mahin.core.database.entity.ContractionSessionEntity
import dev.mahin.core.database.entity.CycleProfileEntity
import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.database.entity.KickEventEntity
import dev.mahin.core.database.entity.KickSessionEntity
import dev.mahin.core.database.entity.PeriodDayEntity
import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.core.database.entity.PregnancyAppointmentEntity
import dev.mahin.core.database.entity.PregnancyDatingRevisionEntity
import dev.mahin.core.database.entity.PregnancyDayLogEntity
import dev.mahin.core.database.entity.PregnancyRecordEntity
import dev.mahin.core.database.entity.TtcDayLogEntity

@Database(
    entities = [
        AppMetaEntity::class,
        CycleProfileEntity::class,
        PeriodRecordEntity::class,
        PeriodDayEntity::class,
        DailyLogEntity::class,
        TtcDayLogEntity::class,
        PregnancyRecordEntity::class,
        PregnancyDatingRevisionEntity::class,
        PregnancyDayLogEntity::class,
        PregnancyAppointmentEntity::class,
        KickSessionEntity::class,
        KickEventEntity::class,
        ContractionSessionEntity::class,
        ContractionEventEntity::class,
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

    abstract fun ttcDayLogDao(): TtcDayLogDao

    abstract fun pregnancyRecordDao(): PregnancyRecordDao

    abstract fun pregnancyDatingRevisionDao(): PregnancyDatingRevisionDao

    abstract fun pregnancyDayLogDao(): PregnancyDayLogDao

    abstract fun pregnancyAppointmentDao(): PregnancyAppointmentDao

    abstract fun kickSessionDao(): KickSessionDao

    abstract fun kickEventDao(): KickEventDao

    abstract fun contractionSessionDao(): ContractionSessionDao

    abstract fun contractionEventDao(): ContractionEventDao

    companion object {
        const val VERSION: Int = 4
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
