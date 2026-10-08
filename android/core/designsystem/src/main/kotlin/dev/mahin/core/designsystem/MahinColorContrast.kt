package dev.mahin.core.designsystem

import androidx.compose.ui.graphics.Color
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

fun contrastRatioBetweenColors(
    foreground: Color,
    background: Color,
): Double = contrastRatio(colorToHex(foreground), colorToHex(background))

fun colorToHex(color: Color): String {
    val r = (color.red * 255f).toInt().coerceIn(0, 255)
    val g = (color.green * 255f).toInt().coerceIn(0, 255)
    val b = (color.blue * 255f).toInt().coerceIn(0, 255)
    return "#%02X%02X%02X".format(r, g, b)
}

fun hexToColor(hex: String): Color {
    val digits = hex.removePrefix("#")
    val r = digits.substring(0, 2).toInt(16)
    val g = digits.substring(2, 4).toInt(16)
    val b = digits.substring(4, 6).toInt(16)
    return Color(0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong())
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

/** Alpha-composites [foregroundHex] over [backgroundHex] in sRGB (straight alpha). */
fun compositeHexOver(
    foregroundHex: String,
    foregroundAlpha: Float,
    backgroundHex: String,
): String {
    fun channel(
        hex: String,
        start: Int,
    ): Int = hex.removePrefix("#").substring(start, start + 2).toInt(16)

    val a = foregroundAlpha.coerceIn(0f, 1f)
    val br = channel(backgroundHex, 0)
    val bg = channel(backgroundHex, 2)
    val bb = channel(backgroundHex, 4)
    val fr = channel(foregroundHex, 0)
    val fg = channel(foregroundHex, 2)
    val fb = channel(foregroundHex, 4)

    fun blend(
        f: Int,
        b: Int,
    ): Int = ((f * a) + (b * (1f - a))).toInt().coerceIn(0, 255)

    return "#%02X%02X%02X".format(blend(fr, br), blend(fg, bg), blend(fb, bb))
}
