package dev.mahin.domain.reminders

import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.cycle.CyclePredictionEngineV1
import dev.mahin.domain.cycle.CyclePredictionInput
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderSchedulePlannerTest {
    private val zone = ZoneId.of("Asia/Tehran")
    private val today = LocalDate.of(2026, 3, 10)

    @Test
    fun periodUpcoming_firesDayBeforeEarliestPrediction() {
        val prediction =
            CyclePredictionEngineV1.predict(
                CyclePredictionInput(
                    today = today,
                    completedCycles = emptyList(),
                    openPeriodStart = today.minusDays(14),
                    typicalCycleLengthDays = 28,
                    regularity = dev.mahin.core.model.CycleRegularity.REGULAR,
                ),
            )
        assertTrue(prediction.nextPeriod != null)
        val input =
            ReminderScheduleInput(
                zoneId = zone.id,
                now = Instant.parse("2026-03-01T05:00:00Z"),
                periodUpcomingEnabled = true,
            )
        val plan =
            ReminderSchedulePlanner.planPeriodUpcoming(
                prediction = prediction,
                zone = zone,
                input = input,
            )
        assertTrue(plan != null)
        assertEquals(ReminderCategory.PERIOD_UPCOMING, plan!!.category)
    }

    @Test
    fun pregnancyWeekly_schedulesNextWeekBoundary() {
        val lmp = LocalDate.of(2026, 1, 1)
        val input =
            ReminderScheduleInput(
                zoneId = zone.id,
                now = Instant.parse("2026-03-10T04:00:00Z"),
                pregnancyWeeklyEnabled = true,
            )
        val plan =
            ReminderSchedulePlanner.planPregnancyWeekly(
                pregnancyId = "p1",
                lmpDate = lmp,
                zone = zone,
                input = input,
                today = today,
            )
        assertTrue(plan != null)
        assertEquals(ReminderCategory.PREGNANCY_WEEKLY, plan!!.category)
    }

    @Test
    fun planAll_respectsModeForTtcOnly() {
        val input =
            ReminderScheduleInput(
                zoneId = zone.id,
                now = today.atStartOfDay(zone).toInstant(),
                ttcLoggingEnabled = true,
            )
        val cycleOnly =
            ReminderSchedulePlanner.planAll(
                input = input,
                context =
                    ReminderPlanContext(
                        reproductiveMode = ReproductiveMode.CYCLE_TRACKING,
                        prediction = null,
                        pregnancyLmpDate = null,
                        activePregnancyId = null,
                        appointments = emptyList(),
                        openPeriodStart = null,
                        onPeriodToday = false,
                    ),
            )
        assertTrue(cycleOnly.isEmpty())
        val ttc =
            ReminderSchedulePlanner.planAll(
                input = input,
                context =
                    ReminderPlanContext(
                        reproductiveMode = ReproductiveMode.TRYING_TO_CONCEIVE,
                        prediction = null,
                        pregnancyLmpDate = null,
                        activePregnancyId = null,
                        appointments = emptyList(),
                        openPeriodStart = null,
                        onPeriodToday = false,
                    ),
            )
        assertTrue(ttc.any { it.category == ReminderCategory.TTC_LOGGING })
    }

    @Test
    fun appointmentReminder_skipsWhenDisabled() {
        val input =
            ReminderScheduleInput(
                zoneId = zone.id,
                now = Instant.parse("2026-03-10T06:00:00Z"),
                appointmentEnabled = true,
            )
        val futureMs = Instant.parse("2026-03-12T10:00:00Z").toEpochMilli()
        val plans =
            ReminderSchedulePlanner.planAppointments(
                appointments =
                    listOf(
                        AppointmentReminderSeed(
                            appointmentId = "a1",
                            scheduledAtEpochMs = futureMs,
                            reminderEnabled = false,
                            descriptiveFa = "ویزیت",
                        ),
                    ),
                zone = zone,
                input = input,
            )
        assertTrue(plans.isEmpty())
    }

    @Test
    fun timezone_resolveFallsBackForInvalidId() {
        val resolved = ReminderTimezone.resolveZone("Not/AZone")
        assertTrue(resolved.id.isNotEmpty())
    }
}
