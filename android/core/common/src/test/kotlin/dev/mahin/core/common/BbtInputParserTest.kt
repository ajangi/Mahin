package dev.mahin.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BbtInputParserTest {
    @Test
    fun parse_persianDigitsAndDecimalSeparator() {
        val result = BbtInputParser.parse("۳۶٫۵")
        assertThat(result).isInstanceOf(BbtInputParser.ParseResult.Valid::class.java)
        assertThat((result as BbtInputParser.ParseResult.Valid).celsius).isWithin(0.001).of(36.5)
    }

    @Test
    fun parse_arabicIndicDigits() {
        val result = BbtInputParser.parse("٣٦.٥")
        assertThat(result).isInstanceOf(BbtInputParser.ParseResult.Valid::class.java)
    }

    @Test
    fun parse_outOfRange_isInvalid() {
        assertThat(BbtInputParser.parse("34.0")).isInstanceOf(BbtInputParser.ParseResult.Invalid::class.java)
        assertThat(BbtInputParser.parse("43")).isInstanceOf(BbtInputParser.ParseResult.Invalid::class.java)
    }

    @Test
    fun parse_garbage_isInvalid() {
        assertThat(BbtInputParser.parse("abc")).isInstanceOf(BbtInputParser.ParseResult.Invalid::class.java)
    }

    @Test
    fun parse_empty_isEmpty() {
        assertThat(BbtInputParser.parse("")).isEqualTo(BbtInputParser.ParseResult.Empty)
    }

    @Test
    fun parse_nonFinite_isInvalid() {
        assertThat(BbtInputParser.parse("NaN")).isInstanceOf(BbtInputParser.ParseResult.Invalid::class.java)
        assertThat(BbtInputParser.parse("Infinity")).isInstanceOf(BbtInputParser.ParseResult.Invalid::class.java)
    }
}
