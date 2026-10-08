package dev.mahin.android.golden

import dev.mahin.android.cycle.CalendarUiState
import dev.mahin.android.cycle.TodayLoggedChip
import dev.mahin.android.cycle.TodayLoggedSummary
import dev.mahin.android.cycle.TodayUiState
import dev.mahin.android.cycle.TodayUpcomingAppointment
import dev.mahin.android.cycle.TodayWeekDay
import dev.mahin.core.database.cycle.CycleDashboard
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.designsystem.icon.MahinIcons
import dev.mahin.core.model.PregnancyDatingSource
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.cycle.CyclePredictionResult
import dev.mahin.domain.cycle.CycleRingSegment
import dev.mahin.domain.cycle.CycleRingSegmentKind
import dev.mahin.domain.cycle.CycleTodayHero
import dev.mahin.domain.cycle.DateRangeEstimate
import dev.mahin.domain.cycle.PredictionConfidence
import dev.mahin.domain.cycle.TodaySnapshot
import dev.mahin.domain.pregnancy.GestationalAge
import dev.mahin.domain.pregnancy.PregnancyDatingSnapshot
import dev.mahin.domain.pregnancy.PregnancyStatusSnapshot
import dev.mahin.domain.pregnancy.PregnancyTodayHero
import dev.mahin.domain.pregnancy.PregnancyTodaySnapshot
import dev.mahin.domain.pregnancy.PregnancyTrimester
import java.time.LocalDate

object M15GoldenFixtures {
    private val anchor = LocalDate.of(2025, 3, 1)
    private val today = LocalDate.of(2025, 3, 14)
    private val converter = PersianCivilDateConverter

    fun todayFirstDay(): TodayUiState = TodayUiState(todaySnapshot = TodaySnapshot.FirstDay)

    fun todayEarlyCycle(): TodayUiState =
        cycleState(
            cycleDay = 5,
            daysUntil = 23,
            confidence = PredictionConfidence.MEDIUM,
            showChip = false,
            inFertile = false,
            overdue = false,
        )

    fun todayFertileWindow(): TodayUiState =
        cycleState(
            cycleDay = 14,
            daysUntil = 14,
            confidence = PredictionConfidence.MEDIUM,
            showChip = false,
            inFertile = true,
            overdue = false,
        )

    fun todayOverdue(): TodayUiState =
        cycleState(
            cycleDay = 32,
            daysUntil = -4,
            confidence = PredictionConfidence.MEDIUM,
            showChip = false,
            inFertile = false,
            overdue = true,
        )

    fun todayLowConfidence(): TodayUiState =
        cycleState(
            cycleDay = 10,
            daysUntil = 18,
            confidence = PredictionConfidence.LOW,
            showChip = true,
            inFertile = false,
            overdue = false,
        )

    fun todayPregnancyWeek(): TodayUiState {
        val dating =
            PregnancyDatingSnapshot(
                lmpDate = LocalDate.of(2025, 1, 1),
                lmpBasedEdd = LocalDate.of(2025, 10, 8),
                clinicalEddDate = null,
                effectiveEddDate = LocalDate.of(2025, 10, 8),
                datingSource = PregnancyDatingSource.LMP_PLUS_280_DAYS,
            )
        val status =
            PregnancyStatusSnapshot(
                dating = dating,
                gestationalAge = GestationalAge(weeks = 21, days = 3, totalDays = 150),
                trimester = PregnancyTrimester.SECOND,
                daysUntilEdd = 100,
                displayWeekNumber = 22,
            )
        val weekStrip = buildWeekStrip()
        return TodayUiState(
            reproductiveMode = ReproductiveMode.PREGNANT,
            pregnancySnapshot =
                PregnancyTodaySnapshot(
                    hero =
                        PregnancyTodayHero(
                            gestationalWeeks = 21,
                            gestationalDays = 3,
                            trimester = PregnancyTrimester.SECOND,
                            daysUntilEdd = 100,
                            datingSource = PregnancyDatingSource.LMP_PLUS_280_DAYS,
                            displayWeekNumber = 22,
                            totalGestationalDays = 280,
                        ),
                ),
            pregnancyStatus = status,
            weekStrip = weekStrip,
            weekStripWeeks = listOf(weekStrip),
            upcomingAppointment =
                TodayUpcomingAppointment(
                    titleFa = "سونوگرافی",
                    scheduledAtEpochMs = 1_700_000_000_000L,
                ),
        )
    }

    fun calendarMonth(): CalendarUiState = M14aGoldenFixtures.calendarPopulated()

    fun calendarDaySheetOpen(): CalendarUiState = M14aGoldenFixtures.calendarPopulated().copy(daySheetOpen = true)

    @Suppress("LongParameterList")
    private fun cycleState(
        cycleDay: Int,
        daysUntil: Int,
        confidence: PredictionConfidence,
        showChip: Boolean,
        inFertile: Boolean,
        overdue: Boolean,
    ): TodayUiState {
        val hero =
            CycleTodayHero(
                cycleDay = cycleDay,
                cycleLengthDays = 28,
                ringSegments =
                    listOf(
                        CycleRingSegment(CycleRingSegmentKind.LOGGED_PERIOD, 1, 5),
                        CycleRingSegment(CycleRingSegmentKind.PREDICTED_PERIOD, 24, 28),
                        CycleRingSegment(CycleRingSegmentKind.FERTILE_WINDOW, 10, 17),
                    ),
                confidence = confidence,
                showConfidenceChip = showChip,
                daysUntilNextPeriodEarliest = daysUntil,
                isInFertileWindow = inFertile,
                isOverdue = overdue,
            )
        val prediction =
            CyclePredictionResult(
                algorithmVersion = "cycle-prediction-v1",
                confidence = confidence,
                cycleDay = cycleDay,
                nextPeriod =
                    DateRangeEstimate(
                        today.plusDays(daysUntil.toLong()),
                        today.plusDays((daysUntil + 3).toLong()),
                    ),
                fertileWindow =
                    DateRangeEstimate(
                        today.minusDays(3),
                        today.plusDays(3),
                    ),
                estimatedOvulation = null,
                insufficientDataReason = null,
            )
        return TodayUiState(
            dashboard =
                CycleDashboard(
                    profile = null,
                    prediction = prediction,
                    todayLog = null,
                    onPeriodToday = false,
                    openPeriodStart = anchor,
                ),
            todaySnapshot = TodaySnapshot.Cycle(hero),
            weekStrip = buildWeekStrip(),
            weekStripWeeks = listOf(buildWeekStrip()),
            loggedSummary =
                TodayLoggedSummary(
                    chips =
                        listOf(
                            TodayLoggedChip(MahinIcons.Symptom.fatigue, "خستگی"),
                        ),
                ),
        )
    }

    private fun buildWeekStrip(): List<TodayWeekDay> =
        (0..6).map { offset ->
            val date = today.minusDays(3).plusDays(offset.toLong())
            TodayWeekDay(
                date = date,
                jalali = converter.toJalali(date),
                isToday = date == today,
                isSelected = false,
            )
        }
}
