package dev.mahin.domain.healthconnect

import com.google.common.truth.Truth.assertThat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Test

class MenstruationFlowMapperTest {
    @Test
    fun tehranStartOfDayInstant() {
        val instant =
            MenstruationFlowMapper.localDateToInstantStartOfDay(
                LocalDate.of(2024, 1, 15),
                ZoneId.of("Asia/Tehran"),
            )
        assertThat(instant).isEqualTo(Instant.parse("2024-01-14T20:30:00Z"))
    }

    @Test
    fun localDateFromRecordUsesZoneOffsetNotSystemDefault() {
        val instant = Instant.parse("2024-01-14T20:30:00Z")
        val date = MenstruationImportPolicy.localDateFromRecord(instant, ZoneOffset.ofHoursMinutes(3, 30))
        assertThat(date).isEqualTo(LocalDate.of(2024, 1, 15))
    }

    @Test
    fun exportClientRecordIdIsStable() {
        val id = MenstruationExportIds.clientRecordId(LocalDate.of(2024, 3, 2))
        assertThat(id).isEqualTo("mahin-period-day-2024-03-02")
    }

    @Test
    fun importMergerPrefersNewerSource() {
        val incoming = Instant.ofEpochMilli(2_000L)
        assertThat(MenstruationImportMerger.shouldReplaceLocal(1_000L, incoming)).isTrue()
        assertThat(MenstruationImportMerger.shouldReplaceLocal(3_000L, incoming)).isFalse()
    }
}
