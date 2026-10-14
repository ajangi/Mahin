package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "program_session_completion",
    indices = [Index("programId"), Index("sessionId")],
)
data class ProgramSessionCompletionEntity(
    @PrimaryKey val id: String,
    val programId: String,
    val sessionId: String,
    val completedAtEpochMs: Long,
)
