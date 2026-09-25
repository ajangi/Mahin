package dev.mahin.core.common

object LocaleDecimalInputNormalizer {
    fun normalizeToAsciiDecimal(input: String): String {
        val builder = StringBuilder(input.length)
        for (ch in input) {
            val mapped =
                when (ch) {
                    in '0'..'9' -> ch
                    in '\u06F0'..'\u06F9' -> '0' + (ch.code - '\u06F0'.code)
                    in '\u0660'..'\u0669' -> '0' + (ch.code - '\u0660'.code)
                    ',', '\u060C', '\u066B' -> '.'
                    else -> ch
                }
            builder.append(mapped)
        }
        return builder.toString()
    }
}
