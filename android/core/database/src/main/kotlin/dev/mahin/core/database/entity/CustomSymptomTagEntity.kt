package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "custom_symptom_tag",
    indices = [Index(value = ["label"], unique = true)],
)
data class CustomSymptomTagEntity(
    @PrimaryKey val id: String,
    val label: String,
    val createdAtEpochMs: Long,
)
