package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content_program_cache")
data class ContentProgramCacheEntity(
    @PrimaryKey val documentId: String,
    val payloadJson: String,
    val cachedAtEpochMs: Long,
)
