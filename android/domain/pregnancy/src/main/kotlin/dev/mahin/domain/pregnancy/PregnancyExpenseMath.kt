package dev.mahin.domain.pregnancy

import dev.mahin.core.datetime.PersianDigits

enum class PregnancyExpenseCurrencyUnit {
    TOMAN,
    RIAL,
}

data class PregnancyExpenseLine(
    val category: String,
    val amountMinor: Long,
    val currencyUnit: PregnancyExpenseCurrencyUnit,
)

data class PregnancyExpenseCategoryTotal(
    val category: String,
    val totalMinor: Long,
    val currencyUnit: PregnancyExpenseCurrencyUnit,
)

object PregnancyExpenseMath {
    fun summarizeByCategory(lines: List<PregnancyExpenseLine>): List<PregnancyExpenseCategoryTotal> =
        lines
            .groupBy { it.category to it.currencyUnit }
            .map { (key, group) ->
                PregnancyExpenseCategoryTotal(
                    category = key.first,
                    totalMinor = group.sumOf { it.amountMinor },
                    currencyUnit = key.second,
                )
            }.sortedBy { it.category }

    @Suppress("ReturnCount")
    fun parseAmountInput(raw: String): Pair<Long?, PregnancyExpenseAmountError?> {
        val normalized =
            PersianDigits
                .toAscii(raw)
                .trim()
                .replace(",", "")
                .replace("٬", "")
                .replace("،", "")
        if (normalized.isEmpty()) {
            return null to PregnancyExpenseAmountError.EMPTY
        }
        val value =
            normalized.toLongOrNull()
                ?: return null to PregnancyExpenseAmountError.UNPARSEABLE
        if (value == 0L) {
            return null to PregnancyExpenseAmountError.ZERO
        }
        if (value < 0) {
            return null to PregnancyExpenseAmountError.NEGATIVE
        }
        if (value > MAX_AMOUNT_MINOR) {
            return null to PregnancyExpenseAmountError.TOO_LARGE
        }
        return value to null
    }

    fun formatAmount(
        amountMinor: Long,
        unit: PregnancyExpenseCurrencyUnit,
    ): String {
        val digits = PersianDigits.format(amountMinor.toString())
        val suffix =
            when (unit) {
                PregnancyExpenseCurrencyUnit.TOMAN -> "تومان"
                PregnancyExpenseCurrencyUnit.RIAL -> "ریال"
            }
        return "$digits $suffix"
    }

    private const val MAX_AMOUNT_MINOR: Long = 9_999_999_999_999L
}

enum class PregnancyExpenseAmountError {
    EMPTY,
    UNPARSEABLE,
    ZERO,
    NEGATIVE,
    TOO_LARGE,
}
