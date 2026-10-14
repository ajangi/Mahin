package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guided_player_state")
data class GuidedPlayerStateEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val programId: String,
    val sessionId: String,
    val stepIndex: Int,
    val stepElapsedMs: Long,
    val paused: Boolean,
    val updatedAtEpochMs: Long,
) {
    companion object {
        const val SINGLE_ROW_ID: Int = 1
    }
}
