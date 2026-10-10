package dev.mahin.core.designsystem.component

import androidx.compose.ui.graphics.Color

data class MahinCalendarDayDecoration(
    val fillColor: Color? = null,
    val predictedPeriodOutline: Boolean = false,
    val estimatedOvulation: Boolean = false,
    val hasLogEntries: Boolean = false,
    val isToday: Boolean = false,
)
