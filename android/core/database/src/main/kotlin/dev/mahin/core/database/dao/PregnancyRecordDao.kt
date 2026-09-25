package dev.mahin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.mahin.core.database.entity.PregnancyRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PregnancyRecordDao {
    @Query("SELECT * FROM pregnancy_record WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<PregnancyRecordEntity?>

    @Query("SELECT * FROM pregnancy_record WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): PregnancyRecordEntity?

    @Query("SELECT * FROM pregnancy_record ORDER BY createdAtEpochMs DESC")
    suspend fun getAll(): List<PregnancyRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PregnancyRecordEntity)
}
