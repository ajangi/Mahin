package dev.mahin.domain.pregnancy

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PregnancyPlanTimelineMergerTest {
    @Test
    fun merge_ordersByGestationalWeekThenAppointmentTime() {
        val care =
            listOf(
                PregnancyPlanTimelineMerger.CarePlanRow(
                    itemId = "c2",
                    documentId = "doc",
                    title = "سونوگرافی",
                    weekStart = 20,
                    weekEnd = 22,
                    category = "ultrasound",
                ),
                PregnancyPlanTimelineMerger.CarePlanRow(
                    itemId = "c1",
                    documentId = "doc",
                    title = "آزمایش",
                    weekStart = 8,
                    weekEnd = 10,
                    category = "test",
                ),
            )
        val appointments =
            listOf(
                PregnancyPlanTimelineMerger.AppointmentRow(
                    appointmentId = "a1",
                    title = "ویزیت",
                    scheduledAtEpochMs = 200L,
                    gestationalWeekAtVisit = 12,
                ),
            )
        val merged = PregnancyPlanTimelineMerger.merge(care, appointments)
        assertThat(merged[0]).isInstanceOf(PregnancyPlanTimelineMerger.TimelineEntry.CarePlan::class.java)
        assertThat((merged[0] as PregnancyPlanTimelineMerger.TimelineEntry.CarePlan).row.weekStart).isEqualTo(8)
        assertThat(merged[1]).isInstanceOf(PregnancyPlanTimelineMerger.TimelineEntry.Appointment::class.java)
        assertThat(merged[2]).isInstanceOf(PregnancyPlanTimelineMerger.TimelineEntry.CarePlan::class.java)
        assertThat((merged[2] as PregnancyPlanTimelineMerger.TimelineEntry.CarePlan).row.weekStart).isEqualTo(20)
    }

    @Test
    fun merge_careAndAppointmentInSameWeek_ordersCareBeforeAppointment() {
        val care =
            listOf(
                PregnancyPlanTimelineMerger.CarePlanRow(
                    itemId = "c1",
                    documentId = "doc",
                    title = "آزمایش",
                    weekStart = 12,
                    weekEnd = 12,
                    category = "test",
                ),
            )
        val appointments =
            listOf(
                PregnancyPlanTimelineMerger.AppointmentRow(
                    appointmentId = "a1",
                    title = "ویزیت",
                    scheduledAtEpochMs = 100L,
                    gestationalWeekAtVisit = 12,
                ),
            )
        val merged = PregnancyPlanTimelineMerger.merge(care, appointments)
        assertThat(merged).hasSize(2)
        assertThat(merged[0]).isInstanceOf(PregnancyPlanTimelineMerger.TimelineEntry.CarePlan::class.java)
        assertThat(merged[1]).isInstanceOf(PregnancyPlanTimelineMerger.TimelineEntry.Appointment::class.java)
    }
}
