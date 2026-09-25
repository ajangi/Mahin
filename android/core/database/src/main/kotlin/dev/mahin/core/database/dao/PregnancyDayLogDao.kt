package dev.mahin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.mahin.core.database.entity.PregnancyDayLogEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface PregnancyDayLogDao {
    @Query("SELECT * FROM pregnancy_day_log WHERE logDate = :date LIMIT 1")
    suspend fun getForDate(date: LocalDate): PregnancyDayLogEntity?

    @Query("SELECT * FROM pregnancy_day_log WHERE logDate BETWEEN :start AND :end ORDER BY logDate DESC")
    fun observeRange(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<PregnancyDayLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PregnancyDayLogEntity)

    @Query("DELETE FROM pregnancy_day_log WHERE logDate = :date")
    suspend fun deleteByDate(date: LocalDate)
}
