package dev.mahin.android.cycle

import dev.mahin.core.common.BbtInputParser
import dev.mahin.core.common.BloodPressureInputParser
import dev.mahin.core.common.WeightInputParser

internal object LogInputValidators {
    fun parseBbt(raw: String): Pair<Double?, BbtFieldError?> =
        when (val parsed = BbtInputParser.parse(raw)) {
            is BbtInputParser.ParseResult.Empty -> null to null
            is BbtInputParser.ParseResult.Valid -> parsed.celsius to null
            is BbtInputParser.ParseResult.Invalid -> {
                val error =
                    when (parsed.reason) {
                        BbtInputParser.InvalidReason.UNPARSEABLE -> BbtFieldError.UNPARSEABLE
                        BbtInputParser.InvalidReason.OUT_OF_RANGE -> BbtFieldError.OUT_OF_RANGE
                    }
                null to error
            }
        }

    fun parseWeight(raw: String): Pair<Double?, WeightBpFieldError?> =
        when (val parsed = WeightInputParser.parse(raw)) {
            is WeightInputParser.ParseResult.Empty -> null to null
            is WeightInputParser.ParseResult.Valid -> parsed.kilograms to null
            is WeightInputParser.ParseResult.Invalid -> null to weightError(parsed.reason)
        }

    fun parseBloodPressure(
        systolicRaw: String,
        diastolicRaw: String,
    ): Pair<Pair<Int, Int>?, WeightBpFieldError?> =
        when (
            val parsed =
                BloodPressureInputParser.parse(
                    systolicRaw,
                    diastolicRaw,
                )
        ) {
            is BloodPressureInputParser.ParseResult.Empty -> null to null
            is BloodPressureInputParser.ParseResult.Valid ->
                (parsed.systolic to parsed.diastolic) to null
            is BloodPressureInputParser.ParseResult.Invalid -> null to bpError(parsed.reason)
        }

    private fun weightError(reason: WeightInputParser.InvalidReason): WeightBpFieldError =
        when (reason) {
            WeightInputParser.InvalidReason.UNPARSEABLE -> WeightBpFieldError.UNPARSEABLE
            WeightInputParser.InvalidReason.OUT_OF_RANGE -> WeightBpFieldError.OUT_OF_RANGE
        }

    private fun bpError(reason: BloodPressureInputParser.InvalidReason): WeightBpFieldError =
        when (reason) {
            BloodPressureInputParser.InvalidReason.UNPARSEABLE -> WeightBpFieldError.UNPARSEABLE
            BloodPressureInputParser.InvalidReason.OUT_OF_RANGE -> WeightBpFieldError.OUT_OF_RANGE
            BloodPressureInputParser.InvalidReason.SYSTOLIC_NOT_GREATER -> WeightBpFieldError.BP_ORDER
        }
}
