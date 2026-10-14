package dev.mahin.android.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.common.LocalDateClock
import dev.mahin.core.content.DailyTipDataSource
import dev.mahin.core.content.DailyTipFetchResult
import dev.mahin.core.content.DailyTipTargetingKey
import dev.mahin.core.database.cycle.CycleDashboard
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.ContentCachePreferencesRepository
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
        private val dailyTipDataSource: DailyTipDataSource,
        private val contentCachePreferences: ContentCachePreferencesRepository,
        private val localDateClock: LocalDateClock,
    ) : ViewModel() {
        private val converter = PersianCivilDateConverter
        private val _uiState = MutableStateFlow(TodayUiState())
        val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                combine(
                    repository.observeDashboard(localDateClock.today()),
                    repository.observePeriods(),
                ) { dashboard, periods ->
                    val anchor = resolveAnchor(periods, dashboard)
                    val snapshot = buildCycleSnapshot(dashboard, periods, anchor)
                    Triple(dashboard, periods, snapshot)
                }.collect { (dashboard, periods, snapshot) ->
                    val now = localDateClock.today()
                    _uiState.update { state ->
                        state.copy(
                            today = now,
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
                                asOfDate = localDateClock.today(),
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
                            today = localDateClock.today(),
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
                            weekStrip = buildWeekStrip(localDateClock.today()),
                            weekStripWeeks = buildWeekStripWeeks(localDateClock.today()),
                        )
                    }
                }
            }
            viewModelScope.launch {
                combine(
                    repository.observeDashboard(localDateClock.today()),
                    pregnancyRepository.observeProfile(),
                    pregnancyRepository.observeActivePregnancy(),
                ) { dashboard, profile, pregnancy ->
                    Triple(dashboard, profile, pregnancy)
                }.collect { (dashboard, profile, pregnancy) ->
                    refreshDailyTip(dashboard, profile?.reproductiveMode, pregnancy)
                }
            }
            viewModelScope.launch {
                combine(
                    notificationPreferencesRepository.observeSnapshot(),
                    repository.observeDashboard(localDateClock.today()),
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
                today = localDateClock.today(),
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
            val today = localDateClock.today()
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
                val logLines = logSummaryLineForDate(date)
                _uiState.update { state ->
                    state.copy(
                        daySheetDate = date,
                        daySheetMarkers = buildDayMarkers(date, periods, dashboard),
                        daySheetLogLines = logLines,
                        weekStrip = state.weekStrip.map { day -> day.copy(isSelected = day.date == date) },
                        weekStripWeeks =
                            state.weekStripWeeks.map { week ->
                                week.map { day -> day.copy(isSelected = day.date == date) }
                            },
                    )
                }
            }
        }

        fun dismissDaySheet() {
            _uiState.update {
                it.copy(
                    daySheetDate = null,
                    daySheetLogLines = emptyList(),
                    weekStrip = it.weekStrip.map { day -> day.copy(isSelected = false) },
                    weekStripWeeks =
                        it.weekStripWeeks.map { week ->
                            week.map { day -> day.copy(isSelected = false) }
                        },
                )
            }
        }

        fun openConfidenceSheet() {
            _uiState.update { it.copy(showConfidenceSheet = true) }
        }

        fun dismissConfidenceSheet() {
            _uiState.update { it.copy(showConfidenceSheet = false) }
        }

        fun dismissDailyTip() {
            val tip = _uiState.value.dailyTip ?: return
            viewModelScope.launch {
                contentCachePreferences.dismissDailyTip(tip.id, localDateClock.today().toString())
                _uiState.update { it.copy(dailyTip = null) }
            }
        }

        @Suppress("CyclomaticComplexMethod")
        private suspend fun refreshDailyTip(
            dashboard: CycleDashboard?,
            mode: ReproductiveMode?,
            pregnancy: dev.mahin.core.database.entity.PregnancyRecordEntity?,
        ) {
            val reproductiveMode = mode ?: ReproductiveMode.CYCLE_TRACKING
            val apiMode =
                when (reproductiveMode) {
                    ReproductiveMode.TRYING_TO_CONCEIVE -> "ttc"
                    ReproductiveMode.PREGNANT -> "pregnancy"
                    else -> "cycle"
                }
            val cyclePhase =
                if (reproductiveMode == ReproductiveMode.PREGNANT) {
                    null
                } else {
                    DailyTipTargetingMapper.cyclePhase(
                        cycleDay = dashboard?.prediction?.cycleDay,
                        onPeriodToday = dashboard?.onPeriodToday == true,
                        inFertileWindow =
                            dashboard?.prediction?.fertileWindow?.let { range ->
                                val today = localDateClock.today()
                                !today.isBefore(range.earliest) && !today.isAfter(range.latest)
                            } == true,
                    )
                }
            val pregnancyWeek =
                if (reproductiveMode == ReproductiveMode.PREGNANT && pregnancy != null) {
                    PregnancyDatingEngineV1
                        .status(
                            lmpDate = pregnancy.lmpDate,
                            clinicalEddDate = pregnancy.clinicalEddDate,
                            asOfDate = localDateClock.today(),
                        ).displayWeekNumber
                } else {
                    null
                }
            val targetingKey =
                DailyTipTargetingKey(
                    mode = apiMode,
                    cyclePhase = cyclePhase,
                    pregnancyWeek = pregnancyWeek,
                    localDateIso = localDateClock.today().toString(),
                )
            val tip =
                when (val fetch = dailyTipDataSource.fetchDailyTips(targetingKey)) {
                    is DailyTipFetchResult.Success -> {
                        val resolved = fetch.tips.firstOrNull()
                        if (resolved == null) {
                            dailyTipDataSource.clearCachedDailyTip()
                            null
                        } else {
                            dailyTipDataSource.cacheDailyTip(resolved, targetingKey)
                            resolved
                        }
                    }
                    is DailyTipFetchResult.NetworkFailure ->
                        dailyTipDataSource.cachedDailyTipFor(targetingKey)
                }
            val dismissed =
                tip != null &&
                    contentCachePreferences.isDailyTipDismissed(tip.id, localDateClock.today().toString())
            val card =
                if (tip != null && !dismissed) {
                    TodayDailyTipCard(
                        id = tip.id,
                        title = tip.title,
                        summary = tip.summary,
                        placeholder = tip.placeholder,
                    )
                } else {
                    null
                }
            _uiState.update { it.copy(dailyTip = card) }
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

        private suspend fun logSummaryLineForDate(date: LocalDate): List<String> =
            repository.getDailyLogForDate(date)?.toSummaryLine()?.let { listOf(it) } ?: emptyList()

        private fun DailyLogEntity.toSummaryLine(): String? =
            buildList {
                if (symptomTags.isNotBlank()) add(symptomTags)
                if (moodTags.isNotBlank()) add(moodTags)
                if (!note.isNullOrBlank()) add("…")
            }.joinToString(" · ").takeIf { it.isNotBlank() }

        private fun buildDayMarkers(
            date: LocalDate,
            periods: List<PeriodRecordEntity>,
            dashboard: CycleDashboard?,
        ): DayMarkers {
            val prediction = dashboard?.prediction ?: return DayMarkers()
            val logDates =
                buildSet {
                    if (dashboard.todayLog != null) add(localDateClock.today())
                }
            return CycleDayMarkersMapper.forDate(
                date = date,
                today = localDateClock.today(),
                periods = periods,
                prediction = prediction,
                datesWithLogEntries = logDates,
            )
        }
    }
