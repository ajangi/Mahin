package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content_recent_view")
data class ContentRecentViewEntity(
    @PrimaryKey val documentId: String,
    val viewedAtEpochMs: Long,
)
