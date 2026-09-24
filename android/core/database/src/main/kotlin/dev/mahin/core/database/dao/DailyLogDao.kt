package dev.mahin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.mahin.core.database.entity.DailyLogEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_log WHERE logDate = :date LIMIT 1")
    fun observeForDate(date: LocalDate): Flow<DailyLogEntity?>

    @Query("SELECT * FROM daily_log WHERE logDate = :date LIMIT 1")
    suspend fun getForDate(date: LocalDate): DailyLogEntity?

    @Query("SELECT * FROM daily_log WHERE logDate BETWEEN :start AND :end ORDER BY logDate DESC")
    fun observeRange(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_log ORDER BY logDate DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<DailyLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: DailyLogEntity)

    @Query("DELETE FROM daily_log WHERE id = :id")
    suspend fun deleteById(id: String)
}
