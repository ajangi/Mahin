package dev.mahin.android.ttc

import android.content.res.Resources
import dev.mahin.android.R
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.datetime.PersianDigits
import java.util.Locale

/**
 * BBT chart is drawn in chronological order (earlier → later along the X axis).
 * We do not mirror the polyline for RTL because the axis represents time, not layout direction.
 */
object BbtChartA11y {
    fun summary(
        resources: Resources,
        points: List<BbtChartPoint>,
    ): String {
        if (points.isEmpty()) return ""
        val sorted = points.sortedBy { it.date }
        val min = sorted.minOf { it.celsius }
        val max = sorted.maxOf { it.celsius }
        val latest = sorted.last()
        val latestDate = PersianCivilDateConverter.toJalali(latest.date)
        val dateLabel =
            "${PersianDigits.format(latestDate.year)}/" +
                "${PersianDigits.format(latestDate.month)}/" +
                PersianDigits.format(latestDate.day)
        return resources.getString(
            R.string.ttc_bbt_chart_a11y_summary,
            PersianDigits.format(sorted.size.toString()),
            formatCelsius(min),
            formatCelsius(max),
            formatCelsius(latest.celsius),
            dateLabel,
        )
    }

    internal fun formatCelsius(value: Double): String = PersianDigits.format(String.format(Locale.US, "%.2f", value))
}
