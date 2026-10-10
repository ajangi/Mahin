package dev.mahin.core.designsystem.component

import androidx.compose.ui.graphics.Color
import dev.mahin.core.designsystem.compositeSrgbOver
import dev.mahin.core.designsystem.contrastRatioBetweenColors

data class MahinCalendarDayCellColors(
    val label: Color,
    val ovulationMarker: Color?,
)

/** Straight-alpha composite of semi-transparent [markerFill] over [parentBackground] (sRGB). */
fun blendCalendarMarkerFillSrgb(
    parentBackground: Color,
    markerFill: Color,
): Color = compositeSrgbOver(markerFill, parentBackground)

private val labelCandidates =
    listOf(
        Color.White,
        Color.Black.copy(alpha = 0.87f),
    )

private val markerCandidates =
    listOf(
        Color.White,
        Color.Black,
    )

/**
 * Label and ovulation-marker colours for a decorated (non-selected) day cell.
 * [parentBackground] is the visible area behind the cell (not an Oklab lerp target).
 */
fun mahinCalendarDayCellColors(
    parentBackground: Color,
    markerFill: Color,
    estimatedOvulation: Boolean,
): MahinCalendarDayCellColors {
    val blended = blendCalendarMarkerFillSrgb(parentBackground, markerFill)
    val label =
        labelCandidates
            .filter { contrastRatioBetweenColors(it, blended) >= 4.5 }
            .maxByOrNull { contrastRatioBetweenColors(it, blended) }
            ?: labelCandidates.maxBy { contrastRatioBetweenColors(it, blended) }
    val marker =
        if (estimatedOvulation) {
            markerCandidates
                .filter { contrastRatioBetweenColors(it, blended) >= 3.0 }
                .maxByOrNull { contrastRatioBetweenColors(it, blended) }
                ?: markerCandidates.maxBy { contrastRatioBetweenColors(it, blended) }
        } else {
            null
        }
    return MahinCalendarDayCellColors(label = label, ovulationMarker = marker)
}

/** Regression-only luminance threshold (not used in production UI). */
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
