package dev.mahin.android.cycle

import dev.mahin.core.datetime.JalaliDate

data class LogScreenActions(
    val onDateSelected: (JalaliDate) -> Unit,
    val onOpenDayEditor: () -> Unit = {},
)
