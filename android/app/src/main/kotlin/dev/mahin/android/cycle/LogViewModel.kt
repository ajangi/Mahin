package dev.mahin.android.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.android.pregnancy.PregnancyFormatters
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.pregnancy.PregnancyDayLogInput
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.database.ttc.TtcDayLogInput
import dev.mahin.core.database.ttc.TtcTrackingRepository
import dev.mahin.core.datastore.TtcPrivacyPreferencesRepository
import dev.mahin.core.datetime.CivilDateConverter
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.healthconnect.PeriodDayTrackingService
import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PeriodFlowLevel
import dev.mahin.core.model.PregnancyTestResult
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class BbtFieldError {
    UNPARSEABLE,
    OUT_OF_RANGE,
}

data class LogUiState(
    val converter: CivilDateConverter = PersianCivilDateConverter,
    val selectedJalali: JalaliDate = PersianCivilDateConverter.toJalali(LocalDate.now()),
    val reproductiveMode: ReproductiveMode = ReproductiveMode.CYCLE_TRACKING,
    val intercourseLoggingEnabled: Boolean = false,
    val loggingPeriod: Boolean = false,
    val flowLevel: PeriodFlowLevel? = null,
    val symptomTags: Set<String> = emptySet(),
    val moodTags: Set<String> = emptySet(),
    val note: String = "",
    val bbtInput: String = "",
    val bbtError: BbtFieldError? = null,
    val ovulationTest: OvulationTestResult? = null,
    val cervicalMucus: CervicalMucusType? = null,
    val intercourseLogged: Boolean = false,
    val intercourseProtected: Boolean? = null,
    val pregnancyTest: PregnancyTestResult? = null,
    val pregnancySymptomTags: Set<String> = emptySet(),
    val pregnancyWeightInput: String = "",
    val pregnancyWeightError: WeightBpFieldError? = null,
    val pregnancyBpSystolicInput: String = "",
    val pregnancyBpDiastolicInput: String = "",
    val pregnancyBpError: WeightBpFieldError? = null,
    val pregnancyAvailableSymptoms: List<String> =
        listOf("تهوع", "خستگی", "سردرد", "درد کمر", "ورم"),
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
        private val periodDayTrackingService: PeriodDayTrackingService,
        private val ttcRepository: TtcTrackingRepository,
        private val pregnancyRepository: PregnancyTrackingRepository,
        private val ttcPrivacyRepository: TtcPrivacyPreferencesRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LogUiState())
        val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

        private var loadJob: Job? = null
        private var loadGeneration: Int = 0

        init {
            viewModelScope.launch {
                ttcRepository.observeProfile().collect { profile ->
                    val mode = profile?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING
                    _uiState.update { it.copy(reproductiveMode = mode) }
                }
            }
            viewModelScope.launch {
                ttcPrivacyRepository.observeIntercourseLoggingEnabled().collect { enabled ->
                    _uiState.update { state ->
                        state.copy(intercourseLoggingEnabled = enabled)
                    }
                }
            }
            loadForSelectedDate()
        }

        fun onDateSelected(jalali: JalaliDate) {
            _uiState.update { it.copy(selectedJalali = jalali, saved = false, bbtError = null) }
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
            _uiState.update { it.copy(bbtInput = value, bbtError = null, saved = false) }
        }

        fun onOvulationTestSelected(result: OvulationTestResult) {
            _uiState.update { state ->
                val next = if (state.ovulationTest == result) null else result
                state.copy(ovulationTest = next, saved = false)
            }
        }

        fun onCervicalMucusSelected(type: CervicalMucusType) {
            _uiState.update { state ->
                val next = if (state.cervicalMucus == type) null else type
                state.copy(cervicalMucus = next, saved = false)
            }
        }

        fun toggleIntercourse() {
            _uiState.update { state ->
                if (!state.intercourseLoggingEnabled) return@update state
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
            _uiState.update { state ->
                val next = if (state.pregnancyTest == result) null else result
                state.copy(pregnancyTest = next, saved = false)
            }
        }

        fun togglePregnancySymptom(tag: String) {
            _uiState.update { state ->
                val next =
                    if (state.pregnancySymptomTags.contains(tag)) {
                        state.pregnancySymptomTags - tag
                    } else {
                        state.pregnancySymptomTags + tag
                    }
                state.copy(pregnancySymptomTags = next, saved = false)
            }
        }

        fun onPregnancyWeightChange(value: String) {
            _uiState.update { it.copy(pregnancyWeightInput = value, pregnancyWeightError = null, saved = false) }
        }

        fun onPregnancyBpSystolicChange(value: String) {
            _uiState.update { it.copy(pregnancyBpSystolicInput = value, pregnancyBpError = null, saved = false) }
        }

        fun onPregnancyBpDiastolicChange(value: String) {
            _uiState.update { it.copy(pregnancyBpDiastolicInput = value, pregnancyBpError = null, saved = false) }
        }

        fun setIntercourseLoggingEnabled(enabled: Boolean) {
            viewModelScope.launch {
                ttcPrivacyRepository.setIntercourseLoggingEnabled(enabled)
            }
        }

        fun save() {
            viewModelScope.launch {
                performSave(_uiState.value)
            }
        }

        @Suppress("LongMethod")
        internal suspend fun performSave(state: LogUiState) {
            val date = state.converter.toGregorian(state.selectedJalali)
            _uiState.update { it.copy(saving = true, bbtError = null) }
            val reproductiveMode = ttcRepository.getReproductiveMode()
            var bbtCelsius: Double? = null
            var weightKg: Double? = null
            var bpSystolic: Int? = null
            var bpDiastolic: Int? = null
            if (reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE) {
                val (parsedBbt, bbtError) = LogInputValidators.parseBbt(state.bbtInput)
                if (bbtError != null) {
                    _uiState.update { it.copy(saving = false, bbtError = bbtError) }
                    return
                }
                bbtCelsius = parsedBbt
            }
            if (reproductiveMode == ReproductiveMode.PREGNANT) {
                val (parsedWeight, weightError) = LogInputValidators.parseWeight(state.pregnancyWeightInput)
                if (weightError != null) {
                    _uiState.update { it.copy(saving = false, pregnancyWeightError = weightError) }
                    return
                }
                weightKg = parsedWeight
                val (bpPair, bpError) =
                    LogInputValidators.parseBloodPressure(
                        state.pregnancyBpSystolicInput,
                        state.pregnancyBpDiastolicInput,
                    )
                if (bpError != null) {
                    _uiState.update { it.copy(saving = false, pregnancyBpError = bpError) }
                    return
                }
                bpSystolic = bpPair?.first
                bpDiastolic = bpPair?.second
            }
            applyPeriodDayFromLog(state, date)
            repository.upsertDailyLog(
                date = date,
                moodTags = state.moodTags,
                symptomTags = state.symptomTags,
                painSeverity = null,
                note = state.note.takeIf { it.isNotBlank() },
            )
            if (reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE) {
                val existingTtc = ttcRepository.getTtcLogForDate(date)
                val intercourseLogged =
                    if (state.intercourseLoggingEnabled) {
                        state.intercourseLogged
                    } else {
                        existingTtc?.intercourseLogged == true
                    }
                val intercourseProtected =
                    if (state.intercourseLoggingEnabled) {
                        state.intercourseProtected
                    } else {
                        existingTtc?.intercourseProtected
                    }
                ttcRepository.upsertTtcDayLog(
                    TtcDayLogInput(
                        logDate = date,
                        bbtCelsius = bbtCelsius,
                        ovulationTestResult = state.ovulationTest,
                        cervicalMucus = state.cervicalMucus,
                        intercourseLogged = intercourseLogged,
                        intercourseProtected = intercourseProtected,
                        pregnancyTestResult = state.pregnancyTest,
                    ),
                )
            }
            if (reproductiveMode == ReproductiveMode.PREGNANT) {
                pregnancyRepository.upsertPregnancyDayLog(
                    PregnancyDayLogInput(
                        logDate = date,
                        symptomTags = state.pregnancySymptomTags,
                        weightKg = weightKg,
                        bpSystolic = bpSystolic,
                        bpDiastolic = bpDiastolic,
                        note = state.note.takeIf { it.isNotBlank() },
                    ),
                )
            }
            _uiState.update { it.copy(saving = false, saved = true) }
        }

        private suspend fun applyPeriodDayFromLog(
            state: LogUiState,
            date: LocalDate,
        ) {
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
                periodDayTrackingService.saveLoggedPeriodDay(
                    date = date,
                    flowLevel = state.flowLevel,
                    hasClots = false,
                )
            } else if (repository.getPeriodDayForDate(date) != null) {
                periodDayTrackingService.removeUserPeriodDay(date)
            }
        }

        private fun loadForSelectedDate() {
            val generation = ++loadGeneration
            val jalali = _uiState.value.selectedJalali
            loadJob?.cancel()
            loadJob =
                viewModelScope.launch {
                    val date = _uiState.value.converter.toGregorian(jalali)
                    val daily = repository.getDailyLogForDate(date)
                    val periodDay = repository.getPeriodDayForDate(date)
                    val ttc = ttcRepository.getTtcLogForDate(date)
                    val pregnancyLog = pregnancyRepository.getPregnancyDayLogForDate(date)
                    if (generation != loadGeneration) return@launch
                    val symptoms =
                        daily
                            ?.symptomTags
                            ?.split(",")
                            ?.filter { it.isNotBlank() }
                            ?.toSet() ?: emptySet()
                    val moods =
                        daily
                            ?.moodTags
                            ?.split(",")
                            ?.filter { it.isNotBlank() }
                            ?.toSet() ?: emptySet()
                    _uiState.update { current ->
                        current.copy(
                            loggingPeriod = periodDay != null,
                            flowLevel = periodDay?.flowLevel,
                            symptomTags = symptoms,
                            moodTags = moods,
                            note = daily?.note ?: "",
                            bbtInput = ttc?.bbtCelsius?.toString() ?: "",
                            bbtError = null,
                            ovulationTest = ttc?.ovulationTestResult,
                            cervicalMucus = ttc?.cervicalMucus,
                            intercourseLogged = ttc?.intercourseLogged == true,
                            intercourseProtected = ttc?.intercourseProtected,
                            pregnancyTest = ttc?.pregnancyTestResult,
                            pregnancySymptomTags =
                                pregnancyLog
                                    ?.symptomTags
                                    ?.split(",")
                                    ?.filter { it.isNotBlank() }
                                    ?.toSet() ?: emptySet(),
                            pregnancyWeightInput =
                                pregnancyLog?.weightKg?.let { PregnancyFormatters.formatDecimal(it) } ?: "",
                            pregnancyWeightError = null,
                            pregnancyBpSystolicInput = pregnancyLog?.bpSystolic?.toString() ?: "",
                            pregnancyBpDiastolicInput = pregnancyLog?.bpDiastolic?.toString() ?: "",
                            pregnancyBpError = null,
                            saved = false,
                        )
                    }
                }
        }
    }
