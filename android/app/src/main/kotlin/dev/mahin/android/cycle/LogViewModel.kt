package dev.mahin.android.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.datetime.CivilDateConverter
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.model.PeriodFlowLevel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LogUiState(
    val converter: CivilDateConverter = PersianCivilDateConverter,
    val selectedJalali: JalaliDate = PersianCivilDateConverter.toJalali(LocalDate.now()),
    val loggingPeriod: Boolean = false,
    val flowLevel: PeriodFlowLevel? = null,
    val symptomTags: Set<String> = emptySet(),
    val moodTags: Set<String> = emptySet(),
    val note: String = "",
    val saving: Boolean = false,
    val saved: Boolean = false,
    val availableSymptoms: List<String> =
        listOf("گرفتگی", "سردرد", "نفخ", "خستگی", "درد پستان"),
)

@HiltViewModel
class LogViewModel
    @Inject
    constructor(
        private val repository: CycleTrackingRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LogUiState())
        val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

        fun onDateSelected(jalali: JalaliDate) {
            _uiState.update { it.copy(selectedJalali = jalali, saved = false) }
        }

        fun toggleLoggingPeriod() {
            _uiState.update { it.copy(loggingPeriod = !it.loggingPeriod, saved = false) }
        }

        fun onFlowLevelSelected(level: PeriodFlowLevel) {
            _uiState.update { it.copy(flowLevel = level, saved = false) }
        }

        fun toggleSymptom(tag: String) {
            _uiState.update { state ->
                val next =
                    if (state.symptomTags.contains(tag)) {
                        state.symptomTags - tag
                    } else {
                        state.symptomTags + tag
                    }
                state.copy(symptomTags = next, saved = false)
            }
        }

        fun onNoteChange(note: String) {
            _uiState.update { it.copy(note = note, saved = false) }
        }

        fun save() {
            val state = _uiState.value
            val date = state.converter.toGregorian(state.selectedJalali)
            viewModelScope.launch {
                _uiState.update { it.copy(saving = true) }
                if (state.loggingPeriod) {
                    val existing =
                        repository.getAllPeriods().find { record ->
                            !date.isBefore(record.startDate) &&
                                (record.endDate == null || !date.isAfter(record.endDate))
                        }
                    if (existing == null) {
                        repository.upsertPeriod(
                            startDate = date,
                            endDate = date,
                            note = null,
                            recordId = null,
                        )
                    }
                    repository.upsertPeriodDay(
                        date = date,
                        flowLevel = state.flowLevel,
                        hasClots = false,
                    )
                }
                repository.upsertDailyLog(
                    date = date,
                    moodTags = state.moodTags,
                    symptomTags = state.symptomTags,
                    painSeverity = null,
                    note = state.note.takeIf { it.isNotBlank() },
                )
                _uiState.update { it.copy(saving = false, saved = true) }
            }
        }
    }
