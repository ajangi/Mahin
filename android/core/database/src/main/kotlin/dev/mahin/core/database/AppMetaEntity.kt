package dev.mahin.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Schema bootstrap only. Health entities are introduced in M2 with migrations.
 * Do not store reproductive facts in this table.
 */
@Entity(tableName = "app_meta")
data class AppMetaEntity(
    @PrimaryKey val key: String,
    val value: String,
)
