package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.mahin.core.model.PregnancyDatingSource
import dev.mahin.core.model.PregnancyOutcome
import java.time.LocalDate

@Entity(tableName = "pregnancy_record")
data class PregnancyRecordEntity(
    @PrimaryKey val id: String,
    val lmpDate: LocalDate,
    val clinicalEddDate: LocalDate?,
    val effectiveEddDate: LocalDate,
    val datingSource: PregnancyDatingSource,
    val isActive: Boolean,
    val outcome: PregnancyOutcome?,
    val outcomeRecordedAtEpochMs: Long?,
    val suppressCelebratoryNotifications: Boolean,
    val wantsSupportContent: Boolean?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)
