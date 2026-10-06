package dev.mahin.core.designsystem

import kotlin.math.pow

/** WCAG 2.x relative luminance for sRGB hex colours (`#RRGGBB`). */
fun relativeLuminance(hex: String): Double {
    fun channel(value: Int): Double {
        val c = value / 255.0
        return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }
    val digits = hex.removePrefix("#")
    val r = channel(digits.substring(0, 2).toInt(16))
    val g = channel(digits.substring(2, 4).toInt(16))
    val b = channel(digits.substring(4, 6).toInt(16))
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

fun contrastRatio(
    foregroundHex: String,
    backgroundHex: String,
): Double {
    val l1 = relativeLuminance(foregroundHex)
    val l2 = relativeLuminance(backgroundHex)
    val lighter = maxOf(l1, l2)
    val darker = minOf(l1, l2)
    return (lighter + 0.05) / (darker + 0.05)
}
