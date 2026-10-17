package dev.mahin.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pregnancy_expense",
    indices = [Index("expenseDate")],
)
data class PregnancyExpenseEntity(
    @PrimaryKey val id: String,
    val category: String,
    val amountMinor: Long,
    val currencyUnit: String,
    val expenseDate: String,
    val note: String?,
    val updatedAtEpochMs: Long,
)
