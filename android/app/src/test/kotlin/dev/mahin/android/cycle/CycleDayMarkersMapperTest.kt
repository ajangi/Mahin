package dev.mahin.android.cycle

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.domain.cycle.CycleDomainModule
import dev.mahin.domain.cycle.CyclePredictionResult
import dev.mahin.domain.cycle.DateRangeEstimate
import dev.mahin.domain.cycle.PredictionConfidence
import java.time.LocalDate
import java.util.UUID
import org.junit.Test

class CycleDayMarkersMapperTest {
    private val today = LocalDate.of(2025, 3, 10)
    private val anchor = LocalDate.of(2025, 3, 1)

    @Test
    fun forDate_mapsEntitySpansToDayMarkers() {
        val period =
            PeriodRecordEntity(
                id = UUID.randomUUID().toString(),
                startDate = anchor,
                endDate = anchor.plusDays(4),
                note = null,
                createdAtEpochMs = 0L,
                updatedAtEpochMs = 0L,
            )
        val markers =
            CycleDayMarkersMapper.forDate(
                date = anchor.plusDays(2),
                today = today,
                periods = listOf(period),
                prediction = prediction(),
                datesWithLogEntries = emptySet(),
            )
        assertThat(markers.loggedPeriod).isTrue()
    }

    @Test
    fun buildMap_returnsDayMarkersPerDate() {
        val map =
            CycleDayMarkersMapper.buildMap(
                rangeStart = anchor,
                rangeEnd = anchor.plusDays(1),
                today = today,
                periods = emptyList(),
                prediction =
                    prediction().copy(
                        fertileWindow = DateRangeEstimate(anchor, anchor.plusDays(1)),
                    ),
                datesWithLogEntries = emptySet(),
            )
        assertThat(map[anchor]?.fertileWindow).isTrue()
    }

    private fun prediction(): CyclePredictionResult =
        CyclePredictionResult(
            algorithmVersion = CycleDomainModule.PREDICTION_ALGORITHM_VERSION,
            confidence = PredictionConfidence.MEDIUM,
            cycleDay = 5,
            nextPeriod = null,
            fertileWindow = null,
            estimatedOvulation = null,
            insufficientDataReason = null,
        )
}
