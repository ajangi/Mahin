package dev.mahin.android.cycle

import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.designsystem.icon.MahinIconSpec
import dev.mahin.domain.cycle.TodaySnapshot
import dev.mahin.domain.pregnancy.PregnancyStatusSnapshot
import dev.mahin.domain.pregnancy.PregnancyTodaySnapshot
import java.time.LocalDate

data class TodayWeekDay(
    val date: LocalDate,
    val jalali: JalaliDate,
    val isToday: Boolean,
    val isSelected: Boolean,
)

data class TodayLoggedChip(
    val icon: MahinIconSpec,
    val label: String,
)

data class TodayLoggedSummary(
    val chips: List<TodayLoggedChip>,
)

data class TodayUiState(
    val reproductiveMode: dev.mahin.core.model.ReproductiveMode =
        dev.mahin.core.model.ReproductiveMode.CYCLE_TRACKING,
    val dashboard: dev.mahin.core.database.cycle.CycleDashboard? = null,
    val todaySnapshot: TodaySnapshot? = null,
    val pregnancySnapshot: PregnancyTodaySnapshot? = null,
    val pregnancyStatus: PregnancyStatusSnapshot? = null,
    val weekStrip: List<TodayWeekDay> = emptyList(),
    val loggedSummary: TodayLoggedSummary? = null,
    val upcomingReminder: TodayUpcomingReminder? = null,
    val upcomingAppointment: TodayUpcomingAppointment? = null,
    /** M17 CMS slot; always hidden in M15 but container is present for layout tests. */
    val showDailyTipSlot: Boolean = false,
    val weekStripWeeks: List<List<TodayWeekDay>> = emptyList(),
    val showConfidenceSheet: Boolean = false,
    val daySheetDate: LocalDate? = null,
    val daySheetMarkers: DayMarkers? = null,
    val postPregnancyTransition: Boolean = false,
    val postTransitionLearnLinkVisible: Boolean = false,
)
