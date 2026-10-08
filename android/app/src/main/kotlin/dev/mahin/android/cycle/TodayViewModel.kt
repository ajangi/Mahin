package dev.mahin.android.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.database.cycle.CycleDashboard
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.NotificationPreferencesRepository
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.cycle.CycleTodaySnapshotInput
import dev.mahin.domain.cycle.PeriodSpanForSnapshot
import dev.mahin.domain.cycle.TodaySnapshotUseCase
import dev.mahin.domain.pregnancy.PregnancyDatingEngineV1
import dev.mahin.domain.pregnancy.PregnancyTodaySnapshotUseCase
import dev.mahin.domain.reminders.AppointmentReminderSeed
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TodayViewModel
    @Inject
    constructor(
        private val repository: CycleTrackingRepository,
        private val pregnancyRepository: PregnancyTrackingRepository,
        private val notificationPreferencesRepository: NotificationPreferencesRepository,
    ) : ViewModel() {
        private val converter = PersianCivilDateConverter
        private val _uiState = MutableStateFlow(TodayUiState())
        val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                combine(
                    repository.observeDashboard(),
                    repository.observePeriods(),
                ) { dashboard, periods ->
                    val anchor = resolveAnchor(periods, dashboard)
                    val snapshot = buildCycleSnapshot(dashboard, periods, anchor)
                    Triple(dashboard, periods, snapshot)
                }.collect { (dashboard, periods, snapshot) ->
                    val now = LocalDate.now()
                    _uiState.update { state ->
                        state.copy(
                            dashboard = dashboard,
                            todaySnapshot = snapshot,
                            loggedSummary = dashboard.todayLog?.let { TodayLoggedSummaryMapper.fromEntity(it) },
                            weekStrip = buildWeekStrip(now),
                            weekStripWeeks = buildWeekStripWeeks(now),
                            daySheetMarkers =
                                state.daySheetDate?.let { date ->
                                    buildDayMarkers(date, periods, dashboard)
                                },
                        )
                    }
                }
            }
            viewModelScope.launch {
                combine(
                    pregnancyRepository.observeProfile(),
                    pregnancyRepository.observeActivePregnancy(),
                ) { profile, pregnancy ->
                    profile to pregnancy
                }.collect { (profile, pregnancy) ->
                    val mode = profile?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING
                    val status =
                        pregnancy?.let {
                            PregnancyDatingEngineV1.status(
                                lmpDate = it.lmpDate,
                                clinicalEddDate = it.clinicalEddDate,
                                asOfDate = LocalDate.now(),
                            )
                        }
                    val learnVisible =
                        if (mode == ReproductiveMode.POST_PREGNANCY_TRANSITION) {
                            pregnancyRepository.postTransitionLearnLinkVisible()
                        } else {
                            false
                        }
                    _uiState.update {
                        it.copy(
                            reproductiveMode = mode,
                            pregnancyStatus = if (mode == ReproductiveMode.PREGNANT) status else null,
                            pregnancySnapshot =
                                if (mode == ReproductiveMode.PREGNANT && status != null) {
                                    PregnancyTodaySnapshotUseCase.fromStatus(status)
                                } else {
                                    null
                                },
                            postPregnancyTransition = mode == ReproductiveMode.POST_PREGNANCY_TRANSITION,
                            postTransitionLearnLinkVisible = learnVisible,
                            weekStrip = buildWeekStrip(LocalDate.now()),
                            weekStripWeeks = buildWeekStripWeeks(LocalDate.now()),
                        )
                    }
                }
            }
            viewModelScope.launch {
                combine(
                    notificationPreferencesRepository.observeSnapshot(),
                    repository.observeDashboard(),
                    pregnancyRepository.observeProfile(),
                    pregnancyRepository.observeActivePregnancy(),
                ) { prefs, dashboard, profile, pregnancy ->
                    val mode = profile?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING
                    val appointmentEntities =
                        if (pregnancy != null && mode == ReproductiveMode.PREGNANT) {
                            pregnancyRepository.upcomingAppointments(
                                pregnancyId = pregnancy.id,
                                fromEpochMs = System.currentTimeMillis(),
                                limit = 8,
                            )
                        } else {
                            emptyList()
                        }
                    val appointmentSeeds =
                        appointmentEntities.map {
                            AppointmentReminderSeed(
                                appointmentId = it.id,
                                scheduledAtEpochMs = it.scheduledAtEpochMs,
                                reminderEnabled = it.reminderEnabled,
                            )
                        }
                    val reminder =
                        TodayReminderSummary.nextUpcoming(
                            snapshot = prefs,
                            dashboard = dashboard,
                            reproductiveMode = mode,
                            pregnancy = pregnancy,
                            appointments = appointmentSeeds,
                        )
                    val nextAppointment =
                        appointmentEntities.firstOrNull()?.let {
                            TodayUpcomingAppointment(
                                titleFa = it.title,
                                scheduledAtEpochMs = it.scheduledAtEpochMs,
                            )
                        }
                    reminder to nextAppointment
                }.collect { (reminder, appointment) ->
                    _uiState.update {
                        it.copy(
                            upcomingReminder = reminder,
                            upcomingAppointment = appointment,
                        )
                    }
                }
            }
        }

        private fun resolveAnchor(
            periods: List<PeriodRecordEntity>,
            dashboard: CycleDashboard,
        ): LocalDate? =
            periods.firstOrNull { it.endDate == null }?.startDate ?: dashboard.openPeriodStart
                ?: periods.maxByOrNull { it.startDate }?.startDate

        private fun buildCycleSnapshot(
            dashboard: CycleDashboard,
            periods: List<PeriodRecordEntity>,
            anchor: LocalDate?,
        ) = TodaySnapshotUseCase.fromCycle(
            CycleTodaySnapshotInput(
                today = LocalDate.now(),
                prediction = dashboard.prediction,
                periodAnchorStart = anchor,
                currentPeriod = resolveCurrentPeriod(periods, anchor),
                typicalPeriodLengthDays = dashboard.profile?.typicalPeriodLengthDays,
                onPeriodToday = dashboard.onPeriodToday,
            ),
        )

        private fun resolveCurrentPeriod(
            periods: List<PeriodRecordEntity>,
            anchor: LocalDate?,
        ): PeriodSpanForSnapshot? {
            val open = periods.firstOrNull { it.endDate == null }
            if (open != null) {
                return PeriodSpanForSnapshot(open.startDate, open.endDate)
            }
            val latest = periods.maxByOrNull { it.startDate } ?: return null
            if (anchor != null && latest.startDate == anchor) {
                return PeriodSpanForSnapshot(latest.startDate, latest.endDate)
            }
            return null
        }

        private fun buildWeekStrip(center: LocalDate) = weekDaysForCenter(center)

        private fun buildWeekStripWeeks(center: LocalDate): List<List<TodayWeekDay>> =
            (-2..2).map { weekOffset ->
                weekDaysForCenter(center.plusWeeks(weekOffset.toLong()))
            }

        private fun weekDaysForCenter(center: LocalDate): List<TodayWeekDay> {
            val today = LocalDate.now()
            return (0..6).map { offset ->
                val date = center.minusDays(3).plusDays(offset.toLong())
                TodayWeekDay(
                    date = date,
                    jalali = converter.toJalali(date),
                    isToday = date == today,
                    isSelected = false,
                )
            }
        }

        fun onWeekDaySelected(date: LocalDate) {
            viewModelScope.launch {
                val periods = repository.observePeriods().first()
                val dashboard = _uiState.value.dashboard
                _uiState.update { state ->
                    state.copy(
                        daySheetDate = date,
                        daySheetMarkers = buildDayMarkers(date, periods, dashboard),
                        weekStrip = state.weekStrip.map { day -> day.copy(isSelected = day.date == date) },
                    )
                }
            }
        }

        fun dismissDaySheet() {
            _uiState.update {
                it.copy(
                    daySheetDate = null,
                    weekStrip = it.weekStrip.map { day -> day.copy(isSelected = false) },
                )
            }
        }

        fun openConfidenceSheet() {
            _uiState.update { it.copy(showConfidenceSheet = true) }
        }

        fun dismissConfidenceSheet() {
            _uiState.update { it.copy(showConfidenceSheet = false) }
        }

        fun resumeCycleTracking() {
            viewModelScope.launch {
                pregnancyRepository.resumeTracking(ReproductiveMode.CYCLE_TRACKING)
            }
        }

        fun resumeTtc() {
            viewModelScope.launch {
                pregnancyRepository.resumeTracking(ReproductiveMode.TRYING_TO_CONCEIVE)
            }
        }

        private fun buildDayMarkers(
            date: LocalDate,
            periods: List<PeriodRecordEntity>,
            dashboard: CycleDashboard?,
        ): DayMarkers {
            val prediction = dashboard?.prediction ?: return DayMarkers()
            val logDates =
                buildSet {
                    if (dashboard.todayLog != null) add(LocalDate.now())
                }
            return CycleDayMarkersMapper.forDate(
                date = date,
                today = LocalDate.now(),
                periods = periods,
                prediction = prediction,
                datesWithLogEntries = logDates,
            )
        }
    }
