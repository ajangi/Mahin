package dev.mahin.android.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.ttc.TtcDayLogInput
import dev.mahin.core.database.ttc.TtcTrackingRepository
import dev.mahin.core.datetime.CivilDateConverter
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PeriodFlowLevel
import dev.mahin.core.model.PregnancyTestResult
import dev.mahin.core.model.ReproductiveMode
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
    val reproductiveMode: ReproductiveMode = ReproductiveMode.CYCLE_TRACKING,
    val loggingPeriod: Boolean = false,
    val flowLevel: PeriodFlowLevel? = null,
    val symptomTags: Set<String> = emptySet(),
    val moodTags: Set<String> = emptySet(),
    val note: String = "",
    val bbtInput: String = "",
    val ovulationTest: OvulationTestResult? = null,
    val cervicalMucus: CervicalMucusType? = null,
    val intercourseLogged: Boolean = false,
    val intercourseProtected: Boolean? = null,
    val pregnancyTest: PregnancyTestResult? = null,
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
        private val ttcRepository: TtcTrackingRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LogUiState())
        val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                ttcRepository.observeProfile().collect { profile ->
                    val mode = profile?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING
                    _uiState.update { it.copy(reproductiveMode = mode) }
                }
            }
            loadForSelectedDate()
        }

        fun onDateSelected(jalali: JalaliDate) {
            _uiState.update { it.copy(selectedJalali = jalali, saved = false) }
            loadForSelectedDate()
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

        fun onBbtChange(value: String) {
            _uiState.update { it.copy(bbtInput = value, saved = false) }
        }

        fun onOvulationTestSelected(result: OvulationTestResult) {
            _uiState.update { it.copy(ovulationTest = result, saved = false) }
        }

        fun onCervicalMucusSelected(type: CervicalMucusType) {
            _uiState.update { it.copy(cervicalMucus = type, saved = false) }
        }

        fun toggleIntercourse() {
            _uiState.update { state ->
                state.copy(
                    intercourseLogged = !state.intercourseLogged,
                    intercourseProtected = if (!state.intercourseLogged) state.intercourseProtected else null,
                    saved = false,
                )
            }
        }

        fun onIntercourseProtectedSelected(protected: Boolean?) {
            _uiState.update { it.copy(intercourseProtected = protected, saved = false) }
        }

        fun onPregnancyTestSelected(result: PregnancyTestResult) {
            _uiState.update { it.copy(pregnancyTest = result, saved = false) }
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
                if (state.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE) {
                    val bbt =
                        state.bbtInput
                            .trim()
                            .replace(',', '.')
                            .toDoubleOrNull()
                    ttcRepository.upsertTtcDayLog(
                        TtcDayLogInput(
                            logDate = date,
                            bbtCelsius = bbt,
                            ovulationTestResult = state.ovulationTest,
                            cervicalMucus = state.cervicalMucus,
                            intercourseLogged = state.intercourseLogged,
                            intercourseProtected = state.intercourseProtected,
                            pregnancyTestResult = state.pregnancyTest,
                        ),
                    )
                }
                _uiState.update { it.copy(saving = false, saved = true) }
            }
        }

        private fun loadForSelectedDate() {
            val state = _uiState.value
            val date = state.converter.toGregorian(state.selectedJalali)
            viewModelScope.launch {
                val ttc = ttcRepository.getTtcLogForDate(date)
                _uiState.update { current ->
                    current.copy(
                        bbtInput = ttc?.bbtCelsius?.toString() ?: "",
                        ovulationTest = ttc?.ovulationTestResult,
                        cervicalMucus = ttc?.cervicalMucus,
                        intercourseLogged = ttc?.intercourseLogged == true,
                        intercourseProtected = ttc?.intercourseProtected,
                        pregnancyTest = ttc?.pregnancyTestResult,
                        saved = false,
                    )
                }
            }
        }
    }
