package dev.mahin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.mahin.core.database.entity.PregnancyAppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PregnancyAppointmentDao {
    @Query(
        """
        SELECT * FROM pregnancy_appointment
        WHERE pregnancyId = :pregnancyId
        ORDER BY scheduledAtEpochMs ASC
        """,
    )
    fun observeForPregnancy(pregnancyId: String): Flow<List<PregnancyAppointmentEntity>>

    @Query(
        """
        SELECT * FROM pregnancy_appointment
        WHERE pregnancyId = :pregnancyId AND scheduledAtEpochMs >= :fromEpochMs
        ORDER BY scheduledAtEpochMs ASC
        LIMIT :limit
        """,
    )
    suspend fun upcoming(
        pregnancyId: String,
        fromEpochMs: Long,
        limit: Int,
    ): List<PregnancyAppointmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PregnancyAppointmentEntity)

    @Query("DELETE FROM pregnancy_appointment WHERE id = :id")
    suspend fun deleteById(id: String)
}
