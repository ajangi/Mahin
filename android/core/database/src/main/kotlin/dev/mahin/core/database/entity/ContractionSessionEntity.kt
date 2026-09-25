package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contraction_session",
    indices = [Index(value = ["pregnancyId"])],
)
data class ContractionSessionEntity(
    @PrimaryKey val id: String,
    val pregnancyId: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long?,
    val updatedAtEpochMs: Long,
)

@Entity(
    tableName = "contraction_event",
    indices = [Index(value = ["sessionId"])],
)
data class ContractionEventEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long?,
)
