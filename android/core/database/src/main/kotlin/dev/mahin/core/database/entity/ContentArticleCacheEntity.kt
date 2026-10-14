package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content_article_cache")
data class ContentArticleCacheEntity(
    @PrimaryKey val documentId: String,
    val payloadJson: String,
    val cachedAtEpochMs: Long,
)
