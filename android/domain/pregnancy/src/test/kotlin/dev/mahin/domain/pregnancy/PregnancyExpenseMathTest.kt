package dev.mahin.domain.pregnancy

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PregnancyExpenseMathTest {
    @Test
    fun summarizeByCategory_groupsAndSumsPerCurrency() {
        val lines =
            listOf(
                PregnancyExpenseLine("tests_imaging", 100_000, PregnancyExpenseCurrencyUnit.TOMAN),
                PregnancyExpenseLine("tests_imaging", 50_000, PregnancyExpenseCurrencyUnit.TOMAN),
                PregnancyExpenseLine("supplies", 1_000_000, PregnancyExpenseCurrencyUnit.RIAL),
            )
        val totals = PregnancyExpenseMath.summarizeByCategory(lines)
        assertThat(totals).hasSize(2)
        assertThat(totals.first { it.category == "tests_imaging" }.totalMinor).isEqualTo(150_000L)
    }

    @Test
    fun formatAmount_usesPersianDigits() {
        val formatted = PregnancyExpenseMath.formatAmount(12500, PregnancyExpenseCurrencyUnit.TOMAN)
        assertThat(formatted).contains("تومان")
        assertThat(formatted).doesNotContain("12500")
    }

    @Test
    fun parseAmount_rejectsEmptyAndNegative() {
        assertThat(PregnancyExpenseMath.parseAmountInput("").second)
            .isEqualTo(PregnancyExpenseAmountError.EMPTY)
        assertThat(PregnancyExpenseMath.parseAmountInput("-1").second)
            .isEqualTo(PregnancyExpenseAmountError.NEGATIVE)
    }

    @Test
    fun parseAmount_acceptsPersianDigits() {
        val (value, error) = PregnancyExpenseMath.parseAmountInput("۱۲۳۴۵")
        assertThat(error).isNull()
        assertThat(value).isEqualTo(12345L)
    }

    @Test
    fun parseAmount_rejectsZero() {
        assertThat(PregnancyExpenseMath.parseAmountInput("0").second)
            .isEqualTo(PregnancyExpenseAmountError.ZERO)
    }

    @Test
    fun parseAmount_stripsPersianGroupingSeparators() {
        val (value, error) = PregnancyExpenseMath.parseAmountInput("۱٬۲۳۴٬۵۶۷")
        assertThat(error).isNull()
        assertThat(value).isEqualTo(1_234_567L)
    }

    @Test
    fun parseAmount_stripsArabicCommaSeparator() {
        val (value, error) = PregnancyExpenseMath.parseAmountInput("۱،۲۳۴،۵۶۷")
        assertThat(error).isNull()
        assertThat(value).isEqualTo(1_234_567L)
    }
}
