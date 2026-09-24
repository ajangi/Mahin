package dev.mahin.android.ttc

import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.datetime.PersianDigits

/**
 * BBT chart is drawn in chronological order (earlier → later along the X axis).
 * We do not mirror the polyline for RTL because the axis represents time, not layout direction.
 */
object BbtChartA11y {
    fun summary(points: List<BbtChartPoint>): String {
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
        return "count=${sorted.size};min=$min;max=$max;latest=$latest.celsius@$dateLabel"
    }
}
