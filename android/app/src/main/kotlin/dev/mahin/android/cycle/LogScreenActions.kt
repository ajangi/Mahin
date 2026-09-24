package dev.mahin.android.cycle

import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.model.PeriodFlowLevel

data class LogScreenActions(
    val onDateSelected: (JalaliDate) -> Unit,
    val onToggleLoggingPeriod: () -> Unit,
    val onFlowLevelSelected: (PeriodFlowLevel) -> Unit,
    val onToggleSymptom: (String) -> Unit,
    val onNoteChange: (String) -> Unit,
    val onSave: () -> Unit,
    val ttcCallbacks: TtcLogFormCallbacks,
)
