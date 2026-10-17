package dev.mahin.core.datetime

/**
 * Presentation helper for Persian (Eastern Arabic) digits. Canonical persisted values stay ASCII.
 */
object PersianDigits {
    private const val PERSIAN_ZERO = '\u06F0'

    fun format(value: Int): String = format(value.toString())

    fun format(value: Long): String = format(value.toString())

    fun format(text: String): String =
        buildString(text.length) {
            for (ch in text) {
                append(
                    when (ch) {
                        in '0'..'9' -> PERSIAN_ZERO + (ch.code - '0'.code)
                        else -> ch
                    },
                )
            }
        }

    fun toAscii(text: String): String =
        buildString(text.length) {
            for (ch in text) {
                append(
                    when (ch) {
                        in PERSIAN_ZERO..'\u06F9' -> '0' + (ch.code - PERSIAN_ZERO.code)
                        in '\u0660'..'\u0669' -> '0' + (ch.code - '\u0660'.code)
                        else -> ch
                    },
                )
            }
        }
}
