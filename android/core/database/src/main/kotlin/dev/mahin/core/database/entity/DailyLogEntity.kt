package dev.mahin.core.database.entity

import androidx.room.ColumnInfo
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
    val medicationTags: String,
    /** Local-only custom symptom selections; excluded from sync/export. */
    @ColumnInfo(defaultValue = "") val localCustomSymptomTags: String,
    val painSeverity: Int?,
    val painLocation: String?,
    val painNote: String?,
    val note: String?,
    @ColumnInfo(defaultValue = "0") val sleepLogged: Boolean,
    @ColumnInfo(defaultValue = "0") val exerciseLogged: Boolean,
    @ColumnInfo(defaultValue = "0") val waterGlasses: Int,
    val stressLevel: Int?,
    val updatedAtEpochMs: Long,
)
