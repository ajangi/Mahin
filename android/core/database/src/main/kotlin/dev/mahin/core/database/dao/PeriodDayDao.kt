package dev.mahin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.mahin.core.database.entity.PeriodDayEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodDayDao {
    @Query("SELECT * FROM period_day WHERE logDate = :date LIMIT 1")
    suspend fun getForDate(date: LocalDate): PeriodDayEntity?

    @Query("SELECT * FROM period_day WHERE logDate BETWEEN :start AND :end")
    fun observeRange(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<PeriodDayEntity>>

    @Query("SELECT * FROM period_day WHERE logDate BETWEEN :start AND :end")
    suspend fun getRange(
        start: LocalDate,
        end: LocalDate,
    ): List<PeriodDayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: PeriodDayEntity)

    @Query("DELETE FROM period_day WHERE logDate = :date")
    suspend fun deleteByDate(date: LocalDate)
}
