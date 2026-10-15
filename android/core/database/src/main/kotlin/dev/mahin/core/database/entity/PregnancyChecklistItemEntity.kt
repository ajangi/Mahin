package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Local-only hospital-bag checklist; not synced or exported (M18a). */
@Entity(
    tableName = "pregnancy_checklist_item",
    indices = [Index("checklistKind")],
)
data class PregnancyChecklistItemEntity(
    @PrimaryKey val id: String,
    val checklistKind: String,
    val section: String,
    val label: String,
    val sortOrder: Int,
    val checked: Boolean,
    val templateItemId: String?,
    val updatedAtEpochMs: Long,
)
