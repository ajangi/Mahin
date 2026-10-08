package dev.mahin.android.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.datastore.CalendarUiPreferencesRepository
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.domain.cycle.DateRangeEstimate
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
    val legendExpanded: Boolean = true,
    val daySheetOpen: Boolean = false,
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
                    _uiState.update { it.copy(legendExpanded = !collapsed) }
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
                    val markers = mutableMapOf<LocalDate, DayMarkers>()
                    periods.forEach { record ->
                        var day = record.startDate
                        val end = record.endDate ?: record.startDate
                        while (!day.isAfter(end)) {
                            markers[day] = (markers[day] ?: DayMarkers()).copy(loggedPeriod = true)
                            day = day.plusDays(1)
                        }
                    }
                    dashboard.prediction.nextPeriod?.let { range ->
                        addRange(markers, range) { existing ->
                            existing.copy(predictedPeriod = true)
                        }
                    }
                    dashboard.prediction.fertileWindow?.let { range ->
                        addRange(markers, range) { existing ->
                            existing.copy(fertileWindow = true)
                        }
                    }
                    dashboard.prediction.estimatedOvulation?.let { range ->
                        addRange(markers, range) { existing ->
                            existing.copy(estimatedOvulation = true)
                        }
                    }
                    logs.forEach { log ->
                        val existing = markers[log.logDate] ?: DayMarkers()
                        markers[log.logDate] = existing.copy(hasLogEntries = true)
                    }
                    markers
                }.collect { markers ->
                    _uiState.update { it.copy(dayMarkers = markers) }
                }
            }
        }

        fun selectJalaliDate(date: JalaliDate) {
            _uiState.update {
                it.copy(
                    selectedJalali = date,
                    daySheetOpen = true,
                )
            }
        }

        fun dismissDaySheet() {
            _uiState.update { it.copy(daySheetOpen = false) }
        }

        fun setVisibleMonth(month: JalaliDate) {
            _uiState.update { it.copy(visibleMonth = month) }
        }

        fun jumpToToday() {
            val jalali = converter.toJalali(LocalDate.now())
            _uiState.update {
                it.copy(
                    selectedJalali = jalali,
                    visibleMonth = jalali,
                )
            }
        }

        fun toggleLegendExpanded() {
            viewModelScope.launch {
                val expanded = !_uiState.value.legendExpanded
                calendarUiPreferencesRepository.setLegendCollapsed(!expanded)
            }
        }

        private fun addRange(
            markers: MutableMap<LocalDate, DayMarkers>,
            range: DateRangeEstimate,
            transform: (DayMarkers) -> DayMarkers,
        ) {
            var day = range.earliest
            while (!day.isAfter(range.latest)) {
                val existing = markers[day] ?: DayMarkers()
                markers[day] = transform(existing)
                day = day.plusDays(1)
            }
        }
    }
