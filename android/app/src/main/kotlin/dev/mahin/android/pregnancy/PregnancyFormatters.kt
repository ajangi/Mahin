package dev.mahin.android.pregnancy

import dev.mahin.core.datetime.PersianDigits
import dev.mahin.domain.pregnancy.PregnancyTrimester
import java.util.Locale

object PregnancyFormatters {
    fun formatDecimal(value: Double): String {
        val ascii = String.format(Locale.US, "%.1f", value)
        return PersianDigits.format(ascii).replace('.', '٫')
    }

    fun formatInteger(value: Int): String = PersianDigits.format(value)

    fun formatLong(value: Long): String = PersianDigits.format(value.toString())

    fun formatDurationSeconds(totalSeconds: Long): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "${PersianDigits.format(minutes)}:${PersianDigits.format(seconds.toString().padStart(2, '0'))}"
    }
}

fun pregnancyTrimesterLabelRes(trimester: PregnancyTrimester): Int =
    when (trimester) {
        PregnancyTrimester.FIRST -> dev.mahin.android.R.string.pregnancy_trimester_first
        PregnancyTrimester.SECOND -> dev.mahin.android.R.string.pregnancy_trimester_second
        PregnancyTrimester.THIRD -> dev.mahin.android.R.string.pregnancy_trimester_third
    }
