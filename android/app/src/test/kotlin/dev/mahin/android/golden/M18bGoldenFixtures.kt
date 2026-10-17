package dev.mahin.android.golden

import dev.mahin.android.pregnancy.PregnancyExpenseCategorySummaryUi
import dev.mahin.android.pregnancy.PregnancyExpenseRowUi
import dev.mahin.android.pregnancy.PregnancyExpensesUiState
import dev.mahin.android.pregnancy.PregnancyWarningSignItemUi
import dev.mahin.android.pregnancy.PregnancyWarningSignsUiState
import dev.mahin.domain.pregnancy.PregnancyBpFlagLevel

object M18bGoldenFixtures {
    fun expenses(): PregnancyExpensesUiState =
        PregnancyExpensesUiState(
            isLoading = false,
            expenses =
                listOf(
                    PregnancyExpenseRowUi(
                        id = "1",
                        category = "tests_imaging",
                        amountLabel = "۲۵۰٬۰۰۰ تومان",
                        dateLabel = "۱۴۰۴/۰۳/۱۲",
                        note = "سونوگرافی",
                    ),
                ),
            categorySummaries =
                listOf(
                    PregnancyExpenseCategorySummaryUi("tests_imaging", "۲۵۰٬۰۰۰ تومان"),
                ),
        )

    fun warningSigns(): PregnancyWarningSignsUiState =
        PregnancyWarningSignsUiState(
            isLoading = false,
            introTitle = "هشدارهای مهم (نمونه)",
            introBody = "در صورت بروز علائم نگران‌کننده با مراقب خود تماس بگیرید.",
            signs =
                listOf(
                    PregnancyWarningSignItemUi(
                        title = "سردرد شدید",
                        summary = "سردردی که با استراحت بهتر نمی‌شود.",
                    ),
                ),
            escalationTitle = "چه زمانی مراقبت فوری؟",
            escalationBody = "علائم ناگهانی یا تشدیدشده را جدی بگیرید.",
            contactClinicianLabel = "با پزشک یا ماما تماس بگیرید",
            bpFlagLevel = PregnancyBpFlagLevel.ELEVATED,
            bpFlagMessage = "با پزشک یا ماما تماس بگیرید",
        )
}
