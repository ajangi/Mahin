package dev.mahin.domain.pregnancy

/**
 * Merges CMS care-plan schedule rows with user appointments for the Plan timeline.
 * Ordering: gestational week ascending, then scheduled time ascending; care-plan rows precede
 * appointments at the same week when weeks tie.
 */
object PregnancyPlanTimelineMerger {
    data class CarePlanRow(
        val itemId: String,
        val documentId: String,
        val title: String,
        val weekStart: Int,
        val weekEnd: Int,
        val category: String,
    )

    data class AppointmentRow(
        val appointmentId: String,
        val title: String,
        val scheduledAtEpochMs: Long,
        val gestationalWeekAtVisit: Int,
    )

    sealed interface TimelineEntry {
        val sortWeek: Int
        val sortKey: Long

        data class CarePlan(
            val row: CarePlanRow,
        ) : TimelineEntry {
            override val sortWeek: Int = row.weekStart
            override val sortKey: Long = 0L
        }

        data class Appointment(
            val row: AppointmentRow,
        ) : TimelineEntry {
            override val sortWeek: Int = row.gestationalWeekAtVisit
            override val sortKey: Long = row.scheduledAtEpochMs
        }
    }

    fun merge(
        carePlan: List<CarePlanRow>,
        appointments: List<AppointmentRow>,
    ): List<TimelineEntry> {
        val careEntries = carePlan.map { TimelineEntry.CarePlan(it) }
        val appointmentEntries = appointments.map { TimelineEntry.Appointment(it) }
        return (careEntries + appointmentEntries).sortedWith(
            compareBy<TimelineEntry> { it.sortWeek }.thenBy { it.sortKey },
        )
    }
}
