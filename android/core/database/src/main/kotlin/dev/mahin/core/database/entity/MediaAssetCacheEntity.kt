package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_asset_cache")
data class MediaAssetCacheEntity(
    @PrimaryKey val assetId: String,
    val payloadJson: String,
    val cachedAtEpochMs: Long,
)
