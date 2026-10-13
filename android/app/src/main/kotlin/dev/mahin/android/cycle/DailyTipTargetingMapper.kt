package dev.mahin.android.cycle

internal object DailyTipTargetingMapper {
    fun cyclePhase(
        cycleDay: Int?,
        onPeriodToday: Boolean,
        inFertileWindow: Boolean,
    ): String? =
        when {
            onPeriodToday || (cycleDay != null && cycleDay <= 5) -> "menstrual"
            inFertileWindow -> "ovulation"
            cycleDay == null -> null
            cycleDay < 14 -> "follicular"
            else -> "luteal"
        }
}
