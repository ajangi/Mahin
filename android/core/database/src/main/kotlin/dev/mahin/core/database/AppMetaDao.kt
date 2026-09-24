package dev.mahin.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppMetaDao {
    @Query("SELECT * FROM app_meta WHERE `key` = :key LIMIT 1")
    suspend fun find(key: String): AppMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppMetaEntity)
}
