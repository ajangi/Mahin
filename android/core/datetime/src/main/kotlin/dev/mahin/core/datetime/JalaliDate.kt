package dev.mahin.core.datetime

/**
 * Presentation/input type for Jalali calendar. Never persist this as the canonical
 * medical/timeline date; convert at UI/API boundaries.
 */
data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int,
) {
    init {
        require(month in 1..12) { "Jalali month out of range" }
        require(day in 1..31) { "Jalali day out of range" }
        require(year in 1200..1600) { "Jalali year out of expected range" }
    }
}
