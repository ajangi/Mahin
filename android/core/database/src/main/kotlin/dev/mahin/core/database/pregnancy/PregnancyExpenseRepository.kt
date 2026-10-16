package dev.mahin.core.database.pregnancy

import dev.mahin.core.database.MahinDatabase
import dev.mahin.core.database.entity.PregnancyExpenseEntity
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class PregnancyExpenseRepository
    @Inject
    constructor(
        database: MahinDatabase,
    ) {
        private val dao = database.pregnancyExpenseDao()

        fun observeAll(): Flow<List<PregnancyExpenseEntity>> = dao.observeAll()

        @Suppress("LongParameterList")
        suspend fun upsertExpense(
            id: String?,
            category: String,
            amountMinor: Long,
            currencyUnit: String,
            expenseDate: LocalDate,
            note: String?,
        ) {
            val trimmedCategory = category.trim()
            if (trimmedCategory.isEmpty() || amountMinor < 0) return
            val unit = currencyUnit.trim().lowercase().ifBlank { "toman" }
            val now = System.currentTimeMillis()
            dao.upsert(
                PregnancyExpenseEntity(
                    id = id ?: UUID.randomUUID().toString(),
                    category = trimmedCategory,
                    amountMinor = amountMinor,
                    currencyUnit = unit,
                    expenseDate = expenseDate.toString(),
                    note = note?.trim()?.takeIf { it.isNotEmpty() },
                    updatedAtEpochMs = now,
                ),
            )
        }

        suspend fun deleteExpense(id: String) {
            dao.deleteById(id)
        }

        companion object {
            val DEFAULT_CATEGORIES =
                listOf(
                    "prenatal_care",
                    "tests_imaging",
                    "hospital_birth",
                    "supplies",
                    "other",
                )
        }
    }
