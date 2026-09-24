package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.mahin.core.model.PeriodFlowLevel
import java.time.LocalDate

@Entity(tableName = "period_day")
data class PeriodDayEntity(
    @PrimaryKey val logDate: LocalDate,
    val flowLevel: PeriodFlowLevel?,
    val hasClots: Boolean,
    val updatedAtEpochMs: Long,
)
