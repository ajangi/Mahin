package dev.mahin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.mahin.core.database.entity.PregnancyChecklistItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PregnancyChecklistDao {
    @Query("SELECT * FROM pregnancy_checklist_item WHERE checklistKind = :kind ORDER BY section, sortOrder")
    fun observeByKind(kind: String): Flow<List<PregnancyChecklistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<PregnancyChecklistItemEntity>)

    @Query("UPDATE pregnancy_checklist_item SET checked = :checked, updatedAtEpochMs = :updatedAt WHERE id = :id")
    suspend fun setChecked(
        id: String,
        checked: Boolean,
        updatedAt: Long,
    )

    @Query("DELETE FROM pregnancy_checklist_item WHERE checklistKind = :kind")
    suspend fun deleteByKind(kind: String)

    @Query("SELECT COUNT(*) FROM pregnancy_checklist_item WHERE checklistKind = :kind")
    suspend fun countByKind(kind: String): Int
}
