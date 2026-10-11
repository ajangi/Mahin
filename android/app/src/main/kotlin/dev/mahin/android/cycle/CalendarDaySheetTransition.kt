package dev.mahin.android.cycle

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.compositionLocalOf

/** Provided while the calendar day sheet is visible for shared-element bounds. */
internal val LocalCalendarDaySheetTransitionScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

internal fun calendarDaySharedContentKey(date: java.time.LocalDate): String = "calendar-day-$date"
