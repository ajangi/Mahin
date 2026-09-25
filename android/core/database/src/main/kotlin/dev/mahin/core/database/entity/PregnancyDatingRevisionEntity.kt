package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.mahin.core.model.PregnancyDatingSource
import java.time.LocalDate

@Entity(
    tableName = "pregnancy_dating_revision",
    indices = [Index(value = ["pregnancyId"])],
)
data class PregnancyDatingRevisionEntity(
    @PrimaryKey val id: String,
    val pregnancyId: String,
    val effectiveEddDate: LocalDate,
    val datingSource: PregnancyDatingSource,
    val reason: String?,
    val changedAtEpochMs: Long,
)
