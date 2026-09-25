package dev.mahin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import dev.mahin.core.database.entity.PregnancyDatingRevisionEntity

@Dao
interface PregnancyDatingRevisionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PregnancyDatingRevisionEntity)
}
