package dev.mahin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.mahin.core.database.entity.KickEventEntity
import dev.mahin.core.database.entity.KickSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KickSessionDao {
    @Query("SELECT * FROM kick_session WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): KickSessionEntity?

    @Query("SELECT * FROM kick_session WHERE pregnancyId = :pregnancyId ORDER BY startedAtEpochMs DESC")
    fun observeForPregnancy(pregnancyId: String): Flow<List<KickSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: KickSessionEntity)
}

@Dao
interface KickEventDao {
    @Query("SELECT COUNT(*) FROM kick_event WHERE sessionId = :sessionId")
    suspend fun countForSession(sessionId: String): Int

    @Query("SELECT * FROM kick_event WHERE sessionId = :sessionId ORDER BY recordedAtEpochMs ASC")
    suspend fun eventsForSession(sessionId: String): List<KickEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: KickEventEntity)
}
