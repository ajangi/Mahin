package dev.mahin.android.cycle

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.datetime.PersianDigits
import dev.mahin.domain.cycle.DateRangeEstimate
import dev.mahin.domain.cycle.PredictionConfidence
import java.time.LocalDate

object CycleFormatters {
    private val converter = PersianCivilDateConverter

    fun formatLocalDate(date: LocalDate): String {
        val jalali = converter.toJalali(date)
        return "${PersianDigits.format(
            jalali.year,
        )}/${PersianDigits.format(jalali.month)}/${PersianDigits.format(jalali.day)}"
    }

    fun formatRange(range: DateRangeEstimate): String =
        "${formatLocalDate(range.earliest)} – ${formatLocalDate(range.latest)}"
}

@Composable
fun confidenceLabel(confidence: PredictionConfidence): String =
    when (confidence) {
        PredictionConfidence.INSUFFICIENT_DATA -> stringResource(R.string.confidence_insufficient)
        PredictionConfidence.LOW -> stringResource(R.string.confidence_low)
        PredictionConfidence.MEDIUM -> stringResource(R.string.confidence_medium)
        PredictionConfidence.HIGH -> stringResource(R.string.confidence_high)
    }
