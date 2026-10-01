package dev.mahin.domain.healthconnect

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.PeriodFlowLevel
import java.time.Instant
import java.time.LocalDate
import org.junit.Test

class MenstruationFlowMapperTest {
    @Test
    fun mapsPeriodFlowLevelsToHealthConnectFlows() {
        assertThat(MenstruationFlowMapper.toHealthConnectFlow(PeriodFlowLevel.MEDIUM))
            .isEqualTo(HealthConnectMenstruationFlow.MEDIUM)
        assertThat(MenstruationFlowMapper.toHealthConnectFlow(PeriodFlowLevel.VERY_HEAVY))
            .isEqualTo(HealthConnectMenstruationFlow.HEAVY)
    }

    @Test
    fun importMergerPrefersNewerSource() {
        val incoming = Instant.ofEpochMilli(2_000L)
        assertThat(MenstruationImportMerger.shouldReplaceLocal(1_000L, incoming)).isTrue()
        assertThat(MenstruationImportMerger.shouldReplaceLocal(3_000L, incoming)).isFalse()
    }

    @Test
    fun roundTripKnownFlows() {
        val level =
            MenstruationFlowMapper.toPeriodFlowLevel(HealthConnectMenstruationFlow.MEDIUM)
        assertThat(level).isEqualTo(PeriodFlowLevel.MEDIUM)
    }

    @Test
    fun unknownFlowDoesNotCreatePeriodLevel() {
        assertThat(MenstruationFlowMapper.toPeriodFlowLevel(HealthConnectMenstruationFlow.UNKNOWN)).isNull()
    }

    @Test
    fun localDateInstantUsesZoneStartOfDay() {
        val instant =
            MenstruationFlowMapper.localDateToInstantStartOfDay(
                LocalDate.of(2024, 1, 15),
                java.time.ZoneId.of("Asia/Tehran"),
            )
        assertThat(instant).isNotNull()
    }
}
