package dev.mahin.android.cycle

import java.time.LocalDate

data class TodayScreenActions(
    val onOpenLogForDate: (LocalDate) -> Unit = {},
    val onOpenLogTab: () -> Unit = {},
    val onOpenPlan: () -> Unit = {},
    val onOpenPregnancyTab: () -> Unit = {},
    val onOpenCalendar: () -> Unit = {},
)
