package dev.mahin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.mahin.core.database.entity.CycleProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleProfileDao {
    @Query("SELECT * FROM cycle_profile WHERE id = :id LIMIT 1")
    fun observeProfile(id: Int = CycleProfileEntity.SINGLETON_ID): Flow<CycleProfileEntity?>

    @Query("SELECT * FROM cycle_profile WHERE id = :id LIMIT 1")
    suspend fun getProfile(id: Int = CycleProfileEntity.SINGLETON_ID): CycleProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: CycleProfileEntity)
}
