package dev.mahin.android.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.common.LocalDateClock
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.database.entity.PeriodRecordEntity
import dev.mahin.core.datastore.CalendarUiPreferencesRepository
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalendarUiState(
    val todayGregorian: LocalDate = LocalDate.EPOCH,
    val selectedJalali: JalaliDate,
    val visibleMonth: JalaliDate,
    val dayMarkers: Map<LocalDate, DayMarkers> = emptyMap(),
    val dayLogs: Map<LocalDate, List<String>> = emptyMap(),
    val legendExpanded: Boolean = true,
    val daySheetOpen: Boolean = false,
    val legendAutoCollapsedOnce: Boolean = false,
    val periodRecords: List<PeriodRecordEntity> = emptyList(),
    val periodEditId: String? = null,
    val periodEditStartJalali: JalaliDate? = null,
    val periodEditEndJalali: JalaliDate? = null,
)

@HiltViewModel
class CalendarViewModel
    @Inject
    constructor(
        private val repository: CycleTrackingRepository,
        private val calendarUiPreferencesRepository: CalendarUiPreferencesRepository,
        private val localDateClock: LocalDateClock,
    ) : ViewModel() {
        private val converter = PersianCivilDateConverter
        private val today = localDateClock.today()
        private val _uiState =
            MutableStateFlow(
                CalendarUiState(
                    todayGregorian = today,
                    selectedJalali = converter.toJalali(today),
                    visibleMonth = converter.toJalali(today),
                ),
            )
        val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                combine(
                    calendarUiPreferencesRepository.observeLegendCollapsed(),
                    calendarUiPreferencesRepository.observeLegendAutoCollapsedOnce(),
                ) { collapsed, autoOnce ->
                    collapsed to autoOnce
                }.collect { (collapsed, autoOnce) ->
                    _uiState.update {
                        it.copy(
                            legendExpanded = !collapsed,
                            legendAutoCollapsedOnce = autoOnce,
                        )
                    }
                }
            }
            viewModelScope.launch {
                repository.observePeriods().collect { periods ->
                    _uiState.update {
                        it.copy(periodRecords = periods.sortedByDescending { record -> record.startDate })
                    }
                }
            }
            viewModelScope.launch {
                val rangeStart = today.minusMonths(4)
                val rangeEnd = today.plusMonths(4)
                combine(
                    repository.observeDashboard(),
                    repository.observePeriods(),
                    repository.observeDailyLogs(rangeStart, rangeEnd),
                ) { dashboard, periods, logs ->
                    val logDates = logs.map { it.logDate }.toSet()
                    val markers =
                        CycleDayMarkersMapper.buildMap(
                            rangeStart = rangeStart,
                            rangeEnd = rangeEnd,
                            today = today,
                            periods = periods,
                            prediction = dashboard.prediction,
                            datesWithLogEntries = logDates,
                        )
                    val logSummaries =
                        logs.groupBy { it.logDate }.mapValues { (_, entries) ->
                            entries.map { it.toSummaryLine() }
                        }
                    markers to logSummaries
                }.collect { (markers, logSummaries) ->
                    _uiState.update { it.copy(dayMarkers = markers, dayLogs = logSummaries) }
                }
            }
        }

        fun selectJalaliDate(date: JalaliDate) {
            _uiState.update {
                it.copy(
                    selectedJalali = date,
                    visibleMonth = JalaliDate(date.year, date.month, 1),
                    daySheetOpen = true,
                )
            }
            maybeAutoCollapseLegend()
        }

        fun dismissDaySheet() {
            _uiState.update { it.copy(daySheetOpen = false) }
        }

        fun onVisibleMonthChanged(month: JalaliDate) {
            _uiState.update { it.copy(visibleMonth = JalaliDate(month.year, month.month, 1)) }
        }

        fun jumpToToday() {
            val jalali = converter.toJalali(localDateClock.today())
            _uiState.update { applyJumpToToday(it, jalali) }
        }

        fun toggleLegendExpanded() {
            viewModelScope.launch {
                val expanded = !_uiState.value.legendExpanded
                calendarUiPreferencesRepository.setLegendCollapsed(!expanded)
            }
        }

        fun beginPeriodEdit(recordId: String) {
            val record = _uiState.value.periodRecords.find { it.id == recordId } ?: return
            _uiState.update {
                it.copy(
                    periodEditId = recordId,
                    periodEditStartJalali = converter.toJalali(record.startDate),
                    periodEditEndJalali = record.endDate?.let { end -> converter.toJalali(end) },
                )
            }
        }

        fun onPeriodEditStartSelected(jalali: JalaliDate) {
            _uiState.update { it.copy(periodEditStartJalali = jalali) }
        }

        fun onPeriodEditEndSelected(jalali: JalaliDate) {
            _uiState.update { it.copy(periodEditEndJalali = jalali) }
        }

        fun clearPeriodEditEnd() {
            _uiState.update { it.copy(periodEditEndJalali = null) }
        }

        fun cancelPeriodEdit() {
            _uiState.update {
                it.copy(
                    periodEditId = null,
                    periodEditStartJalali = null,
                    periodEditEndJalali = null,
                )
            }
        }

        fun savePeriodEdit(recordId: String) {
            val state = _uiState.value
            val startJalali = state.periodEditStartJalali ?: return
            val start = converter.toGregorian(startJalali)
            val end = state.periodEditEndJalali?.let { converter.toGregorian(it) }
            if (end != null && end.isBefore(start)) return
            viewModelScope.launch {
                val record = repository.getAllPeriods().find { it.id == recordId } ?: return@launch
                repository.upsertPeriod(
                    startDate = start,
                    endDate = end,
                    note = record.note,
                    recordId = recordId,
                )
                cancelPeriodEdit()
            }
        }

        fun mergePeriodWithNext(recordId: String) {
            viewModelScope.launch {
                val periods = repository.getAllPeriods().sortedBy { it.startDate }
                val index = periods.indexOfFirst { it.id == recordId }
                if (index < 0 || index >= periods.lastIndex) return@launch
                repository.mergeAdjacentPeriods(recordId, periods[index + 1].id)
            }
        }

        fun splitPeriod(recordId: String) {
            viewModelScope.launch {
                val record = repository.getAllPeriods().find { it.id == recordId } ?: return@launch
                val end = record.endDate ?: return@launch
                val spanDays = ChronoUnit.DAYS.between(record.startDate, end)
                if (spanDays < 1) return@launch
                val splitAfter = record.startDate.plusDays(spanDays / 2)
                repository.splitPeriodRecord(recordId, splitAfter)
            }
        }

        private fun maybeAutoCollapseLegend() {
            viewModelScope.launch {
                if (!_uiState.value.legendAutoCollapsedOnce && _uiState.value.legendExpanded) {
                    calendarUiPreferencesRepository.markLegendAutoCollapsedOnce()
                }
            }
        }

        private fun DailyLogEntity.toSummaryLine(): String =
            buildList {
                if (symptomTags.isNotBlank()) add(symptomTags)
                if (moodTags.isNotBlank()) add(moodTags)
                if (!note.isNullOrBlank()) add("…")
            }.joinToString(" · ")

        companion object {
            internal fun applyJumpToToday(
                state: CalendarUiState,
                todayJalali: JalaliDate,
            ): CalendarUiState =
                state.copy(
                    selectedJalali = todayJalali,
                    visibleMonth = JalaliDate(todayJalali.year, todayJalali.month, 1),
                )
        }
    }
