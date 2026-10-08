package dev.mahin.core.designsystem.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import dev.mahin.core.designsystem.contrastRatioBetweenColors

/** Blends a semi-transparent marker fill over [surface] (same as [JalaliDayCell]). */
fun blendCalendarMarkerFill(
    surface: Color,
    markerFill: Color,
): Color =
    if (markerFill.alpha <= 0f) {
        surface
    } else {
        lerp(surface, markerFill.copy(alpha = 1f), markerFill.alpha)
    }

private val calendarLabelCandidates =
    listOf(
        Color.White,
        Color.Black.copy(alpha = 0.87f),
    )

private val calendarMarkerCandidates =
    listOf(
        Color.White,
        Color.Black,
    )

/** Day number colour on a blended marker fill; requires ≥4.5:1 when possible. */
fun mahinCalendarDayLabelOnBlendedFill(blendedFill: Color): Color =
    calendarLabelCandidates
        .filter { contrastRatioBetweenColors(it, blendedFill) >= 4.5 }
        .maxByOrNull { contrastRatioBetweenColors(it, blendedFill) }
        ?: calendarLabelCandidates.maxBy { contrastRatioBetweenColors(it, blendedFill) }

/** Ovulation corner marker on a blended fill; requires ≥3.0:1 when possible. */
fun mahinCalendarOvulationMarkerOnBlendedFill(blendedFill: Color): Color =
    calendarMarkerCandidates
        .filter { contrastRatioBetweenColors(it, blendedFill) >= 3.0 }
        .maxByOrNull { contrastRatioBetweenColors(it, blendedFill) }
        ?: calendarMarkerCandidates.maxBy { contrastRatioBetweenColors(it, blendedFill) }

/** Legacy luminance threshold (must not be used in production UI). */
internal fun mahinCalendarDayLabelLuminanceThreshold(blendedFill: Color): Color =
    if (blendedFill.luminance() < 0.5f) {
        Color.White
    } else {
        Color.Black.copy(alpha = 0.87f)
    }

private fun Color.luminance(): Float {
    fun channel(c: Float): Float =
        if (c <= 0.03928f) {
            c / 12.92f
        } else {
            ((c + 0.055f) / 1.055f).let { it * it * it }
        }
    val r = channel(red)
    val g = channel(green)
    val b = channel(blue)
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}
