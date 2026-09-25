package dev.mahin.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BloodPressureInputParserTest {
    @Test
    fun parse_acceptsPersianDigits() {
        val result = BloodPressureInputParser.parse("۱۲۰", "۸۰")
        assertThat(result).isInstanceOf(BloodPressureInputParser.ParseResult.Valid::class.java)
        val valid = result as BloodPressureInputParser.ParseResult.Valid
        assertThat(valid.systolic).isEqualTo(120)
        assertThat(valid.diastolic).isEqualTo(80)
    }

    @Test
    fun parse_requiresSystolicGreaterThanDiastolic() {
        val result = BloodPressureInputParser.parse("۸۰", "۱۲۰")
        assertThat(result).isEqualTo(
            BloodPressureInputParser.ParseResult.Invalid(BloodPressureInputParser.InvalidReason.SYSTOLIC_NOT_GREATER),
        )
    }
}
