package dev.mahin.android.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.DailyLogEntity
import dev.mahin.core.datastore.CalendarUiPreferencesRepository
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalendarUiState(
    val selectedJalali: JalaliDate,
    val visibleMonth: JalaliDate,
    val dayMarkers: Map<LocalDate, DayMarkers> = emptyMap(),
    val dayLogs: Map<LocalDate, List<String>> = emptyMap(),
    val legendExpanded: Boolean = true,
    val daySheetOpen: Boolean = false,
    val legendAutoCollapsedOnce: Boolean = false,
)

@HiltViewModel
class CalendarViewModel
    @Inject
    constructor(
        private val repository: CycleTrackingRepository,
        private val calendarUiPreferencesRepository: CalendarUiPreferencesRepository,
    ) : ViewModel() {
        private val converter = PersianCivilDateConverter
        private val today = LocalDate.now()
        private val _uiState =
            MutableStateFlow(
                CalendarUiState(
                    selectedJalali = converter.toJalali(today),
                    visibleMonth = converter.toJalali(today),
                ),
            )
        val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                calendarUiPreferencesRepository.observeLegendCollapsed().collect { collapsed ->
                    _uiState.update {
                        it.copy(
                            legendExpanded = !collapsed,
                            legendAutoCollapsedOnce = collapsed,
                        )
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
            val jalali = converter.toJalali(LocalDate.now())
            _uiState.update {
                it.copy(
                    selectedJalali = jalali,
                    visibleMonth = JalaliDate(jalali.year, jalali.month, 1),
                )
            }
        }

        fun toggleLegendExpanded() {
            viewModelScope.launch {
                val expanded = !_uiState.value.legendExpanded
                calendarUiPreferencesRepository.setLegendCollapsed(!expanded)
            }
        }

        private fun maybeAutoCollapseLegend() {
            viewModelScope.launch {
                if (!_uiState.value.legendAutoCollapsedOnce && _uiState.value.legendExpanded) {
                    calendarUiPreferencesRepository.setLegendCollapsed(true)
                }
            }
        }

        private fun DailyLogEntity.toSummaryLine(): String =
            buildList {
                if (symptomTags.isNotBlank()) add(symptomTags)
                if (moodTags.isNotBlank()) add(moodTags)
                if (!note.isNullOrBlank()) add("…")
            }.joinToString(" · ")
    }
