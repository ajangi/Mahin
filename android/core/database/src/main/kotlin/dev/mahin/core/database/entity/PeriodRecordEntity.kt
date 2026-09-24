package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "period_record",
    indices = [Index(value = ["startDate"]), Index(value = ["endDate"])],
)
data class PeriodRecordEntity(
    @PrimaryKey val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val note: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)
