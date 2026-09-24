package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "daily_log",
    indices = [Index(value = ["logDate"], unique = true)],
)
data class DailyLogEntity(
    @PrimaryKey val id: String,
    val logDate: LocalDate,
    val moodTags: String,
    val symptomTags: String,
    val painSeverity: Int?,
    val note: String?,
    val updatedAtEpochMs: Long,
)
