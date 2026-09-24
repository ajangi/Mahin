package dev.mahin.core.database.cycle

import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.domain.cycle.CompletedCycleSpan
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object CyclePeriodMapper {
    fun toCompletedCycles(records: List<PeriodRecordEntity>): List<CompletedCycleSpan> {
        val sorted = records.sortedBy { it.startDate }
        return sorted
            .filter { it.endDate != null }
            .mapIndexed { index, record ->
                val end = record.endDate!!
                val nextStart = sorted.getOrNull(index + 1)?.startDate
                CompletedCycleSpan(
                    periodStart = record.startDate,
                    periodEnd = end,
                    nextPeriodStart = nextStart,
                )
            }.filter { !it.periodEnd.isBefore(it.periodStart) }
    }

    fun openPeriod(records: List<PeriodRecordEntity>): Pair<LocalDate?, LocalDate?> {
        val latest = records.maxByOrNull { it.startDate }
        if (latest == null) return null to null
        if (latest.endDate == null) {
            return latest.startDate to null
        }
        val daysSinceEnd = ChronoUnit.DAYS.between(latest.endDate, LocalDate.now())
        if (daysSinceEnd <= 0) {
            return latest.startDate to latest.endDate
        }
        return null to null
    }
}
