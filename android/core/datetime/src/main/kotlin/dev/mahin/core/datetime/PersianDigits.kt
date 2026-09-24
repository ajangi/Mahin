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
}
