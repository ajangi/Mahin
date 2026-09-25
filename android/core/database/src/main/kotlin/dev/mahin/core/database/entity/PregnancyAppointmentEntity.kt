package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.mahin.core.model.PregnancyAppointmentType

@Entity(
    tableName = "pregnancy_appointment",
    indices = [Index(value = ["pregnancyId"])],
)
data class PregnancyAppointmentEntity(
    @PrimaryKey val id: String,
    val pregnancyId: String,
    val appointmentType: PregnancyAppointmentType,
    val title: String,
    val scheduledAtEpochMs: Long,
    val location: String?,
    val clinicianName: String?,
    val note: String?,
    val reminderEnabled: Boolean,
    val updatedAtEpochMs: Long,
)
