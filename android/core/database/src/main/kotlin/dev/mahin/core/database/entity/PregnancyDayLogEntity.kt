package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "pregnancy_day_log",
    indices = [Index(value = ["logDate"], unique = true)],
)
data class PregnancyDayLogEntity(
    @PrimaryKey val id: String,
    val logDate: LocalDate,
    val symptomTags: String,
    val weightKg: Double?,
    val bpSystolic: Int?,
    val bpDiastolic: Int?,
    val note: String?,
    val updatedAtEpochMs: Long,
)
