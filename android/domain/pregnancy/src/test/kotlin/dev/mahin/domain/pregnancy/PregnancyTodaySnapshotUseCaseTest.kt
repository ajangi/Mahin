package dev.mahin.domain.pregnancy

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.model.PregnancyDatingSource
import java.time.LocalDate
import org.junit.Test

class PregnancyTodaySnapshotUseCaseTest {
    @Test
    fun mapsGestationalAgeAndTrimester() {
        val lmp = LocalDate.of(2025, 1, 1)
        val status =
            PregnancyDatingEngineV1.status(
                lmpDate = lmp,
                clinicalEddDate = null,
                asOfDate = lmp.plusDays(149),
            )
        val snapshot = PregnancyTodaySnapshotUseCase.fromStatus(status)
        assertThat(snapshot.hero.gestationalWeeks).isEqualTo(21)
        assertThat(snapshot.hero.trimester).isEqualTo(PregnancyTrimester.SECOND)
        assertThat(snapshot.hero.datingSource).isEqualTo(PregnancyDatingSource.LMP_PLUS_280_DAYS)
    }
}
