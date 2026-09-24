package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode

@Entity(tableName = "cycle_profile")
data class CycleProfileEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val reproductiveMode: ReproductiveMode,
    val typicalCycleLengthDays: Int?,
    val typicalPeriodLengthDays: Int?,
    val regularity: CycleRegularity,
    val onboardingCompleted: Boolean,
    val updatedAtEpochMs: Long,
) {
    companion object {
        const val SINGLETON_ID: Int = 1
    }
}
