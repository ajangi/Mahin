package dev.mahin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.mahin.core.database.entity.ContractionEventEntity
import dev.mahin.core.database.entity.ContractionSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContractionSessionDao {
    @Query("SELECT * FROM contraction_session WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ContractionSessionEntity?

    @Query("SELECT * FROM contraction_session WHERE pregnancyId = :pregnancyId ORDER BY startedAtEpochMs DESC")
    fun observeForPregnancy(pregnancyId: String): Flow<List<ContractionSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ContractionSessionEntity)
}

@Dao
interface ContractionEventDao {
    @Query("SELECT * FROM contraction_event WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ContractionEventEntity?

    @Query("SELECT * FROM contraction_event WHERE sessionId = :sessionId ORDER BY startedAtEpochMs ASC")
    suspend fun eventsForSession(sessionId: String): List<ContractionEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ContractionEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ContractionEventEntity)
}
