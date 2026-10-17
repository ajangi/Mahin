package dev.mahin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.mahin.core.database.entity.PregnancyExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PregnancyExpenseDao {
    @Query("SELECT * FROM pregnancy_expense ORDER BY expenseDate DESC, updatedAtEpochMs DESC")
    fun observeAll(): Flow<List<PregnancyExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PregnancyExpenseEntity)

    @Query("DELETE FROM pregnancy_expense WHERE id = :id")
    suspend fun deleteById(id: String)
}
