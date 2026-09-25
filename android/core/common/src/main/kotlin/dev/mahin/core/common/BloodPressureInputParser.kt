package dev.mahin.core.common

/**
 * Parses manual blood-pressure entry as systolic/diastolic integers. No clinical interpretation.
 */
object BloodPressureInputParser {
    const val MIN_MM_HG: Int = 40
    const val MAX_MM_HG: Int = 250

    sealed interface ParseResult {
        data object Empty : ParseResult

        data class Valid(
            val systolic: Int,
            val diastolic: Int,
        ) : ParseResult

        data class Invalid(
            val reason: InvalidReason,
        ) : ParseResult
    }

    enum class InvalidReason {
        UNPARSEABLE,
        OUT_OF_RANGE,
        SYSTOLIC_NOT_GREATER,
    }

    @Suppress("ReturnCount")
    fun parse(
        systolicRaw: String,
        diastolicRaw: String,
    ): ParseResult {
        val sysTrim = systolicRaw.trim()
        val diaTrim = diastolicRaw.trim()
        if (sysTrim.isEmpty() && diaTrim.isEmpty()) return ParseResult.Empty
        val systolic = parseInt(sysTrim) ?: return ParseResult.Invalid(InvalidReason.UNPARSEABLE)
        val diastolic = parseInt(diaTrim) ?: return ParseResult.Invalid(InvalidReason.UNPARSEABLE)
        if (!inRange(systolic) || !inRange(diastolic)) {
            return ParseResult.Invalid(InvalidReason.OUT_OF_RANGE)
        }
        if (systolic <= diastolic) {
            return ParseResult.Invalid(InvalidReason.SYSTOLIC_NOT_GREATER)
        }
        return ParseResult.Valid(systolic = systolic, diastolic = diastolic)
    }

    private fun parseInt(raw: String): Int? {
        val normalized = LocaleDecimalInputNormalizer.normalizeToAsciiDecimal(raw)
        val asDouble = normalized.toDoubleOrNull() ?: return null
        if (!asDouble.isFinite() || asDouble % 1.0 != 0.0) return null
        return asDouble.toInt()
    }

    private fun inRange(value: Int): Boolean = value in MIN_MM_HG..MAX_MM_HG
}
