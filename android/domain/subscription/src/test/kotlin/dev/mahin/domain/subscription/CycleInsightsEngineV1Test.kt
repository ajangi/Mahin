package dev.mahin.domain.subscription

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class CycleInsightsEngineV1Test {
    @Test
    fun freeInsightsExcludePremiumSections() {
        val result =
            CycleInsightsEngineV1.build(
                CycleInsightsInput(
                    periods =
                        listOf(
                            CycleInsightsPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5)),
                            CycleInsightsPeriod(LocalDate.of(2026, 1, 29), LocalDate.of(2026, 2, 2)),
                        ),
                    recentLogs =
                        listOf(
                            CycleInsightsDailyLog(
                                LocalDate.of(2026, 2, 10),
                                symptomTags = listOf("سردرد"),
                                moodTags = emptyList(),
                            ),
                        ),
                    includePremiumSections = false,
                ),
            )
        assertThat(result.premium).isNull()
        assertThat(result.cycleLengthDays).isNotNull()
        assertThat(result.recentSymptomTimeline).isNotEmpty()
    }

    @Test
    fun premiumInsightsIncludeTrendNotes() {
        val result =
            CycleInsightsEngineV1.build(
                CycleInsightsInput(
                    periods =
                        listOf(
                            CycleInsightsPeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5)),
                            CycleInsightsPeriod(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 5)),
                            CycleInsightsPeriod(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 14)),
                        ),
                    recentLogs =
                        listOf(
                            CycleInsightsDailyLog(
                                LocalDate.of(2026, 3, 1),
                                symptomTags = listOf("خستگی"),
                                moodTags = emptyList(),
                            ),
                            CycleInsightsDailyLog(
                                LocalDate.of(2026, 3, 2),
                                symptomTags = listOf("خستگی"),
                                moodTags = emptyList(),
                            ),
                        ),
                    includePremiumSections = true,
                ),
            )
        assertThat(result.premium).isNotNull()
        assertThat(result.premium!!.symptomCoOccurrenceNotesFa).isNotEmpty()
    }
}
