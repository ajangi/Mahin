package dev.mahin.core.designsystem

import kotlin.math.pow
import kotlin.math.sqrt

/** CIE76 ΔE between two `#RRGGBB` colours (sRGB → Lab). */
fun deltaE76(
    hex1: String,
    hex2: String,
): Double {
    val lab1 = hexToLab(hex1)
    val lab2 = hexToLab(hex2)
    return sqrt(
        (lab1.first - lab2.first).pow(2) +
            (lab1.second - lab2.second).pow(2) +
            (lab1.third - lab2.third).pow(2),
    )
}

private fun hexToLab(hex: String): Triple<Double, Double, Double> {
    fun channel(value: Int): Double {
        val c = value / 255.0
        return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }
    val digits = hex.removePrefix("#")
    val r = channel(digits.substring(0, 2).toInt(16))
    val g = channel(digits.substring(2, 4).toInt(16))
    val b = channel(digits.substring(4, 6).toInt(16))
    val x = (r * 0.4124564 + g * 0.3575761 + b * 0.1804375) / 0.95047
    val y = (r * 0.2126729 + g * 0.7151522 + b * 0.0721750) / 1.00000
    val z = (r * 0.0193339 + g * 0.1191920 + b * 0.9503041) / 1.08883

    fun f(t: Double): Double = if (t > 0.008856) t.pow(1.0 / 3.0) else (7.787 * t) + (16.0 / 116.0)
    val l = (116.0 * f(y)) - 16.0
    val a = 500.0 * (f(x) - f(y))
    val bLab = 200.0 * (f(y) - f(z))
    return Triple(l, a, bLab)
}
