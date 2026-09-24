package dev.mahin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.mahin.core.database.entity.PeriodRecordEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodRecordDao {
    @Query("SELECT * FROM period_record ORDER BY startDate DESC")
    fun observeAll(): Flow<List<PeriodRecordEntity>>

    @Query("SELECT * FROM period_record ORDER BY startDate DESC")
    suspend fun getAll(): List<PeriodRecordEntity>

    @Query("SELECT * FROM period_record WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PeriodRecordEntity?

    @Query(
        """
        SELECT * FROM period_record
        WHERE startDate <= :date AND (endDate IS NULL OR endDate >= :date)
        LIMIT 1
        """,
    )
    suspend fun findCoveringDate(date: LocalDate): PeriodRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: PeriodRecordEntity)

    @Query("DELETE FROM period_record WHERE id = :id")
    suspend fun deleteById(id: String)
}
