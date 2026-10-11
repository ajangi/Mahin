package dev.mahin.android.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.android.navigation.LogTabDateRequest
import dev.mahin.android.pregnancy.PregnancyFormatters
import dev.mahin.core.common.LocalDateClock
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.database.ttc.TtcTrackingRepository
import dev.mahin.core.datastore.TtcPrivacyPreferencesRepository
import dev.mahin.core.datetime.CivilDateConverter
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.healthconnect.HealthConnectPeriodDayIntegrationGate
import dev.mahin.core.healthconnect.PeriodDayTrackingService
import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PeriodFlowLevel
import dev.mahin.core.model.PregnancyTestResult
import dev.mahin.core.model.ReproductiveMode
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
    val selectedJalali: JalaliDate = JalaliDate(1403, 1, 1),
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
    val selectedDateReady: Boolean = false,
    val availableSymptoms: List<String> =
        listOf("گرفتگی", "سردرد", "نفخ", "خستگی", "درد پستان"),
)

@Suppress("LongParameterList")
@HiltViewModel
class LogViewModel
    @Inject
    constructor(
        private val repository: CycleTrackingRepository,
        @Suppress("UnusedPrivateProperty") private val periodDayTrackingService: PeriodDayTrackingService,
        @Suppress("UnusedPrivateProperty")
        private val healthConnectPeriodDayIntegrationGate: HealthConnectPeriodDayIntegrationGate,
        private val ttcRepository: TtcTrackingRepository,
        private val pregnancyRepository: PregnancyTrackingRepository,
        private val ttcPrivacyRepository: TtcPrivacyPreferencesRepository,
        private val logTabDateRequest: LogTabDateRequest,
        private val localDateClock: LocalDateClock,
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow(
                LogUiState(
                    selectedJalali = PersianCivilDateConverter.toJalali(localDateClock.today()),
                ),
            )
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
            viewModelScope.launch {
                logTabDateRequest.pendingDate.collect { pending ->
                    pending?.let { date ->
                        onDateSelected(PersianCivilDateConverter.toJalali(date))
                        logTabDateRequest.consume()
                    }
                }
            }
            loadForSelectedDate()
        }

        fun onDateSelected(jalali: JalaliDate) {
            _uiState.update {
                it.copy(selectedJalali = jalali, saved = false, bbtError = null, selectedDateReady = false)
            }
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
                            selectedDateReady = true,
                        )
                    }
                }
        }
    }
