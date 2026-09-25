package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "kick_session",
    indices = [Index(value = ["pregnancyId"])],
)
data class KickSessionEntity(
    @PrimaryKey val id: String,
    val pregnancyId: String,
    val startedAtEpochMs: Long,
    val endedAtEpochMs: Long?,
    val updatedAtEpochMs: Long,
)

@Entity(
    tableName = "kick_event",
    indices = [Index(value = ["sessionId"])],
)
data class KickEventEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val recordedAtEpochMs: Long,
)
