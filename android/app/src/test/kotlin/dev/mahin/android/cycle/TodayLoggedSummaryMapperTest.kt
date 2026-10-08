package dev.mahin.android.cycle

import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.designsystem.icon.MahinIcons
import java.time.LocalDate
import java.util.UUID
import org.junit.Test

class TodayLoggedSummaryMapperTest {
    @Test
    fun mapsSymptomAndMoodTagsToM14bIcons() {
        val entity =
            DailyLogEntity(
                id = UUID.randomUUID().toString(),
                logDate = LocalDate.now(),
                symptomTags = "خستگی,سردرد",
                moodTags = "آرام",
                painSeverity = null,
                note = null,
                updatedAtEpochMs = 0L,
            )
        val summary = TodayLoggedSummaryMapper.fromEntity(entity)
        assertThat(summary.chips.map { it.label }).containsExactly("خستگی", "سردرد", "آرام").inOrder()
        assertThat(summary.chips[0].icon).isEqualTo(MahinIcons.Symptom.fatigue)
        assertThat(summary.chips[1].icon).isEqualTo(MahinIcons.Symptom.headache)
        assertThat(summary.chips[2].icon).isEqualTo(MahinIcons.Mood.calm)
    }
}
