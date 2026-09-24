package dev.mahin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.mahin.core.database.entity.TtcDayLogEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface TtcDayLogDao {
    @Query("SELECT * FROM ttc_day_log WHERE logDate = :date LIMIT 1")
    suspend fun getForDate(date: LocalDate): TtcDayLogEntity?

    @Query("SELECT * FROM ttc_day_log WHERE logDate BETWEEN :start AND :end ORDER BY logDate ASC")
    fun observeRange(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<TtcDayLogEntity>>

    @Query("SELECT * FROM ttc_day_log WHERE logDate BETWEEN :start AND :end ORDER BY logDate ASC")
    suspend fun getRange(
        start: LocalDate,
        end: LocalDate,
    ): List<TtcDayLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TtcDayLogEntity)

    @Query("DELETE FROM ttc_day_log WHERE logDate = :date")
    suspend fun deleteByDate(date: LocalDate)
}
