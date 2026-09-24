package dev.mahin.android.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.database.cycle.CycleTrackingRepository
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
    val dayMarkers: Map<LocalDate, DayMarkers> = emptyMap(),
)

@HiltViewModel
class CalendarViewModel
    @Inject
    constructor(
        private val repository: CycleTrackingRepository,
    ) : ViewModel() {
        private val converter = PersianCivilDateConverter
        private val _uiState =
            MutableStateFlow(
                CalendarUiState(
                    selectedJalali = converter.toJalali(LocalDate.now()),
                ),
            )
        val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                combine(
                    repository.observeDashboard(),
                    repository.observePeriods(),
                ) { dashboard, periods ->
                    val markers = mutableMapOf<LocalDate, DayMarkers>()
                    periods.forEach { record ->
                        var day = record.startDate
                        val end = record.endDate ?: record.startDate
                        while (!day.isAfter(end)) {
                            markers[day] = DayMarkers(loggedPeriod = true)
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
                    markers
                }.collect { markers ->
                    _uiState.update { it.copy(dayMarkers = markers) }
                }
            }
        }

        fun selectJalaliDate(date: JalaliDate) {
            _uiState.update { it.copy(selectedJalali = date) }
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
