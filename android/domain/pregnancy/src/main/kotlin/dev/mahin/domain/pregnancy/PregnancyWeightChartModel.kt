package dev.mahin.domain.pregnancy

data class PregnancyWeightChartPoint(
    val gestationalWeek: Int,
    val weightKg: Double,
)

object PregnancyWeightChartModel {
    fun pointsFromLogs(logs: List<Pair<Int, Double>>): List<PregnancyWeightChartPoint> =
        logs
            .filter { (_, weight) -> weight > 0.0 }
            .sortedBy { (week, _) -> week }
            .map { (week, weight) -> PregnancyWeightChartPoint(week, weight) }
}
