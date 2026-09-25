package dev.mahin.core.common

/**
 * Parses pregnancy weight entry (kg). Range is input sanity only, not a clinical judgment.
 */
object WeightInputParser {
    const val MIN_KG: Double = 30.0
    const val MAX_KG: Double = 200.0

    sealed interface ParseResult {
        data object Empty : ParseResult

        data class Valid(
            val kilograms: Double,
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
        val normalized = LocaleDecimalInputNormalizer.normalizeToAsciiDecimal(trimmed)
        val value = normalized.toDoubleOrNull() ?: return ParseResult.Invalid(InvalidReason.UNPARSEABLE)
        return when {
            !value.isFinite() -> ParseResult.Invalid(InvalidReason.UNPARSEABLE)
            value < MIN_KG || value > MAX_KG -> ParseResult.Invalid(InvalidReason.OUT_OF_RANGE)
            else -> ParseResult.Valid(value)
        }
    }
}
