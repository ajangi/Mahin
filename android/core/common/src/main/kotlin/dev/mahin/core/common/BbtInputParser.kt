package dev.mahin.core.common

/**
 * Normalizes locale-specific decimal input for BBT entry. Range check is input sanity only, not medical.
 */
object BbtInputParser {
    const val MIN_CELSIUS: Double = 35.0
    const val MAX_CELSIUS: Double = 42.0

    sealed interface ParseResult {
        data object Empty : ParseResult

        data class Valid(
            val celsius: Double,
        ) : ParseResult

        data class Invalid(
            val reason: InvalidReason,
        ) : ParseResult
    }

    enum class InvalidReason {
        UNPARSEABLE,
        OUT_OF_RANGE,
    }

    fun parse(raw: String): ParseResult {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ParseResult.Empty
        val normalized = normalizeToAsciiDecimal(trimmed)
        val value = normalized.toDoubleOrNull() ?: return ParseResult.Invalid(InvalidReason.UNPARSEABLE)
        return when {
            !value.isFinite() -> ParseResult.Invalid(InvalidReason.UNPARSEABLE)
            value < MIN_CELSIUS || value > MAX_CELSIUS -> ParseResult.Invalid(InvalidReason.OUT_OF_RANGE)
            else -> ParseResult.Valid(value)
        }
    }

    fun normalizeToAsciiDecimal(input: String): String = LocaleDecimalInputNormalizer.normalizeToAsciiDecimal(input)
}
