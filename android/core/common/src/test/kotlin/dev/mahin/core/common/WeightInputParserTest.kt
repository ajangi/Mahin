package dev.mahin.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WeightInputParserTest {
    @Test
    fun parse_acceptsPersianDecimalSeparator() {
        val result = WeightInputParser.parse("۶۵٫۵")
        assertThat(result).isInstanceOf(WeightInputParser.ParseResult.Valid::class.java)
        assertThat((result as WeightInputParser.ParseResult.Valid).kilograms).isEqualTo(65.5)
    }

    @Test
    fun parse_rejectsOutOfRange() {
        val result = WeightInputParser.parse("۲۵")
        assertThat(result)
            .isEqualTo(WeightInputParser.ParseResult.Invalid(WeightInputParser.InvalidReason.OUT_OF_RANGE))
    }
}
