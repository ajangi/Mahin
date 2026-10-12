package dev.mahin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.mahin.core.database.entity.CustomSymptomTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomSymptomTagDao {
    @Query("SELECT * FROM custom_symptom_tag ORDER BY label ASC")
    fun observeAll(): Flow<List<CustomSymptomTagEntity>>

    @Query("SELECT * FROM custom_symptom_tag ORDER BY label ASC")
    suspend fun getAll(): List<CustomSymptomTagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CustomSymptomTagEntity)

    @Query("DELETE FROM custom_symptom_tag WHERE id = :id")
    suspend fun deleteById(id: String)
}
