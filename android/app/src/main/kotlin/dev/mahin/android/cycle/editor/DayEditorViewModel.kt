package dev.mahin.android.cycle.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.android.cycle.BbtFieldError
import dev.mahin.android.cycle.LogInputValidators
import dev.mahin.android.cycle.WeightBpFieldError
import dev.mahin.android.pregnancy.PregnancyFormatters
import dev.mahin.core.common.LocalDateClock
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CustomSymptomTagEntity
import dev.mahin.core.database.logging.CustomSymptomTagRepository
import dev.mahin.core.database.pregnancy.PregnancyDayLogInput
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.database.ttc.TtcDayLogInput
import dev.mahin.core.database.ttc.TtcTrackingRepository
import dev.mahin.core.datastore.LoggingPreferencesRepository
import dev.mahin.core.datastore.TtcPrivacyPreferencesRepository
import dev.mahin.core.healthconnect.HealthConnectPeriodDayIntegrationGate
import dev.mahin.core.healthconnect.PeriodDayTrackingService
import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.LogEditorCategory
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PainLocation
import dev.mahin.core.model.PeriodFlowLevel
import dev.mahin.core.model.PregnancyTestResult
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.cycle.LogCategoryUsageFromLocalLogs
import dev.mahin.domain.cycle.LogCategoryUsageOrder
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class DayEditorUiState(
    val date: LocalDate? = null,
    val loading: Boolean = false,
    val reproductiveMode: ReproductiveMode = ReproductiveMode.CYCLE_TRACKING,
    val orderedCategories: List<LogEditorCategory> = emptyList(),
    val sexualActivityEnabled: Boolean = false,
    val lifestyleEnabled: Map<LogEditorCategory, Boolean> = emptyMap(),
    val showSexualOptIn: Boolean = false,
    val showManageCategories: Boolean = false,
    val periodLogged: Boolean = false,
    val flowLevel: PeriodFlowLevel? = null,
    val symptomTags: Set<String> = emptySet(),
    val localCustomSymptomTags: Set<String> = emptySet(),
    val moodTags: Set<String> = emptySet(),
    val painLocation: PainLocation? = null,
    val painSeverity: Int? = null,
    val painNote: String = "",
    val note: String = "",
    val medicationTags: Set<String> = emptySet(),
    val sleepLogged: Boolean = false,
    val exerciseLogged: Boolean = false,
    val waterGlasses: Int = 0,
    val stressLevel: Int? = null,
    val bbtInput: String = "",
    val bbtError: BbtFieldError? = null,
    val cervicalMucus: CervicalMucusType? = null,
    val ovulationTest: OvulationTestResult? = null,
    val pregnancyTest: PregnancyTestResult? = null,
    val intercourseLogged: Boolean = false,
    val intercourseProtected: Boolean? = null,
    val customSymptoms: List<String> = emptyList(),
    val customSymptomDraft: String = "",
    val availableSymptoms: List<String> = LogDayEditorCatalog.defaultSymptoms,
    val availableMoods: List<String> = LogDayEditorCatalog.defaultMoods,
    val pregnancySymptomTags: Set<String> = emptySet(),
    val availablePregnancySymptoms: List<String> = LogDayEditorCatalog.defaultPregnancySymptoms,
    val pregnancyWeightInput: String = "",
    val pregnancyBpSystolicInput: String = "",
    val pregnancyBpDiastolicInput: String = "",
    val pregnancyWeightError: WeightBpFieldError? = null,
    val pregnancyBpError: WeightBpFieldError? = null,
    val undoMessage: String? = null,
    val confirmPulseKey: String? = null,
    val categoryUsageCounts: Map<LogEditorCategory, Int> = emptyMap(),
    val pinnedCategoryOrder: List<LogEditorCategory> = emptyList(),
    val mostUsedFirst: Boolean = false,
)

data class UndoSnapshot(
    val periodLogged: Boolean,
    val flowLevel: PeriodFlowLevel?,
    val symptomTags: Set<String>,
    val localCustomSymptomTags: Set<String>,
    val moodTags: Set<String>,
    val medicationTags: Set<String>,
    val painLocation: PainLocation?,
    val painSeverity: Int?,
    val painNote: String?,
    val note: String?,
    val sleepLogged: Boolean,
    val exerciseLogged: Boolean,
    val waterGlasses: Int,
    val stressLevel: Int?,
    val bbtInput: String,
    val cervicalMucus: CervicalMucusType?,
    val ovulationTest: OvulationTestResult?,
    val pregnancyTest: PregnancyTestResult?,
    val intercourseLogged: Boolean,
    val intercourseProtected: Boolean?,
    val pregnancySymptomTags: Set<String>,
    val pregnancyWeightInput: String,
    val pregnancyBpSystolicInput: String,
    val pregnancyBpDiastolicInput: String,
)

@HiltViewModel
@Suppress("LongParameterList", "TooManyFunctions", "UnusedPrivateProperty", "LargeClass")
class DayEditorViewModel
    @Inject
    constructor(
        private val cycleRepository: CycleTrackingRepository,
        private val ttcRepository: TtcTrackingRepository,
        private val pregnancyRepository: PregnancyTrackingRepository,
        private val loggingPreferencesRepository: LoggingPreferencesRepository,
        private val ttcPrivacyRepository: TtcPrivacyPreferencesRepository,
        private val periodDayTrackingService: PeriodDayTrackingService,
        private val healthConnectPeriodDayIntegrationGate: HealthConnectPeriodDayIntegrationGate,
        private val customSymptomTagRepository: CustomSymptomTagRepository,
        private val localDateClock: LocalDateClock,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(DayEditorUiState())
        val uiState: StateFlow<DayEditorUiState> = _uiState.asStateFlow()

        private var lastUndo: UndoSnapshot? = null
        private val saveMutex = Mutex()

        init {
            viewModelScope.launch {
                combine(
                    loggingPreferencesRepository.observeSexualActivityEnabled(),
                    loggingPreferencesRepository.observeMostUsedFirst(),
                    loggingPreferencesRepository.observePinnedCategoryOrder(),
                    ttcRepository.observeProfile(),
                ) { sexual, mostUsedFirst, pinned, profile ->
                    val mode = profile?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING
                    Pair(Pair(sexual, mostUsedFirst), Pair(pinned, mode))
                }.collect { (sexualPrefs, pinnedMode) ->
                    val (sexual, mostUsedFirst) = sexualPrefs
                    val (pinned, mode) = pinnedMode
                    _uiState.update { state ->
                        state.copy(
                            sexualActivityEnabled = sexual,
                            reproductiveMode = mode,
                            orderedCategories =
                                buildCategoryOrder(
                                    state = state,
                                    pinned = pinned,
                                    sexualEnabled = sexual,
                                    mode = mode,
                                    mostUsedFirst = mostUsedFirst,
                                    referenceDate = state.date ?: localDateClock.today(),
                                    usageCounts = state.categoryUsageCounts,
                                ),
                            pinnedCategoryOrder = pinned,
                            mostUsedFirst = mostUsedFirst,
                        )
                    }
                }
            }
            viewModelScope.launch {
                combine(
                    loggingPreferencesRepository.observeLifestyleModuleEnabled(LogEditorCategory.SLEEP),
                    loggingPreferencesRepository.observeLifestyleModuleEnabled(LogEditorCategory.EXERCISE),
                    loggingPreferencesRepository.observeLifestyleModuleEnabled(LogEditorCategory.WATER),
                    loggingPreferencesRepository.observeLifestyleModuleEnabled(LogEditorCategory.STRESS),
                ) { sleep, exercise, water, stress ->
                    mapOf(
                        LogEditorCategory.SLEEP to sleep,
                        LogEditorCategory.EXERCISE to exercise,
                        LogEditorCategory.WATER to water,
                        LogEditorCategory.STRESS to stress,
                    )
                }.collect { lifestyle ->
                    _uiState.update { state ->
                        val next = state.copy(lifestyleEnabled = lifestyle)
                        next.copy(
                            orderedCategories =
                                buildCategoryOrder(
                                    state = next,
                                    pinned = next.pinnedCategoryOrder,
                                    sexualEnabled = next.sexualActivityEnabled,
                                    mode = next.reproductiveMode,
                                    mostUsedFirst = next.mostUsedFirst,
                                    referenceDate = next.date ?: localDateClock.today(),
                                    usageCounts = next.categoryUsageCounts,
                                ),
                        )
                    }
                }
            }
            viewModelScope.launch {
                customSymptomTagRepository.observeAll().collect { tags ->
                    val labels = tags.map { it.label }
                    _uiState.update { state ->
                        state.copy(
                            customSymptoms = labels,
                            availableSymptoms = LogDayEditorCatalog.defaultSymptoms,
                        )
                    }
                }
            }
        }

        fun openDate(date: LocalDate) {
            viewModelScope.launch {
                _uiState.update { it.copy(date = date, loading = true) }
                loadDay(date)
            }
        }

        fun dismissUndo() {
            _uiState.update { it.copy(undoMessage = null) }
        }

        fun undoLastChange() {
            val snapshot = lastUndo ?: return
            val date = _uiState.value.date ?: return
            viewModelScope.launch {
                applySnapshot(date, snapshot)
                lastUndo = null
                _uiState.update { it.copy(undoMessage = null) }
                loadDay(date)
            }
        }

        fun setFlowLevel(level: PeriodFlowLevel) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                _uiState.update { it.copy(flowLevel = level) }
                if (_uiState.value.periodLogged) {
                    periodDayTrackingService.applyUserPeriodLogChange(
                        healthConnectIntegrationActive = healthConnectPeriodDayIntegrationGate.isIntegrationActive(),
                        loggingPeriod = true,
                        date = date,
                        flowLevel = level,
                        ensurePeriodSpanExists = { ensurePeriodSpan(date) },
                    )
                }
                pulse("flow_$level", LogEditorCategory.PERIOD_FLOW)
                showUndo()
                loadDay(date)
            }
        }

        fun onBbtChange(value: String) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                val (parsed, error) = LogInputValidators.parseBbt(value)
                _uiState.update { it.copy(bbtInput = value, bbtError = error) }
                if (error == null) {
                    persistTtcLog(date) { it.copy(bbtCelsius = parsed) }
                    pulse("bbt", LogEditorCategory.TTC_TESTS)
                    showUndo()
                }
            }
        }

        fun togglePeriodLogged() {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                val state = _uiState.value
                val next = !state.periodLogged
                val flow = state.flowLevel ?: PeriodFlowLevel.MEDIUM
                if (next) {
                    periodDayTrackingService.applyUserPeriodLogChange(
                        healthConnectIntegrationActive = healthConnectPeriodDayIntegrationGate.isIntegrationActive(),
                        loggingPeriod = true,
                        date = date,
                        flowLevel = flow,
                        ensurePeriodSpanExists = { ensurePeriodSpan(date) },
                    )
                } else {
                    periodDayTrackingService.applyUserPeriodLogChange(
                        healthConnectIntegrationActive = healthConnectPeriodDayIntegrationGate.isIntegrationActive(),
                        loggingPeriod = false,
                        date = date,
                        flowLevel = null,
                        ensurePeriodSpanExists = {},
                    )
                }
                pulse("period", LogEditorCategory.PERIOD_FLOW)
                showUndo()
                loadDay(date)
            }
        }

        fun toggleSymptom(tag: String) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                persistDailyLog(date) { fields ->
                    val next = if (tag in fields.symptomTags) fields.symptomTags - tag else fields.symptomTags + tag
                    fields.copy(symptomTags = next)
                }
                pulse("symptom_$tag", LogEditorCategory.SYMPTOMS)
                showUndo()
                loadDay(date)
            }
        }

        fun toggleLocalCustomSymptom(tag: String) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                persistDailyLog(date) { fields ->
                    val next =
                        if (tag in fields.localCustomSymptomTags) {
                            fields.localCustomSymptomTags - tag
                        } else {
                            fields.localCustomSymptomTags + tag
                        }
                    fields.copy(localCustomSymptomTags = next)
                }
                pulse("custom_symptom_$tag", LogEditorCategory.SYMPTOMS)
                showUndo()
                loadDay(date)
            }
        }

        fun toggleMood(tag: String) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                persistDailyLog(date) { fields ->
                    val next = if (tag in fields.moodTags) fields.moodTags - tag else fields.moodTags + tag
                    fields.copy(moodTags = next)
                }
                pulse("mood_$tag", LogEditorCategory.MOOD)
                showUndo()
                loadDay(date)
            }
        }

        fun toggleMedication(tag: String) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                persistDailyLog(date) { fields ->
                    val next =
                        if (tag in fields.medicationTags) {
                            fields.medicationTags - tag
                        } else {
                            fields.medicationTags + tag
                        }
                    fields.copy(medicationTags = next)
                }
                pulse("medication_$tag", LogEditorCategory.MEDICATION)
                showUndo()
                loadDay(date)
            }
        }

        fun onPregnancyWeightChange(value: String) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                val (parsed, error) = LogInputValidators.parseWeight(value)
                _uiState.update { it.copy(pregnancyWeightInput = value, pregnancyWeightError = error) }
                if (error == null) {
                    persistPregnancyDayLog(date, _uiState.value.copy(pregnancyWeightInput = value))
                    pulse("pregnancy_weight", LogEditorCategory.PREGNANCY_VITALS)
                    showUndo()
                }
            }
        }

        fun onPregnancyBpSystolicChange(value: String) {
            onPregnancyBpChange(systolic = value)
        }

        fun onPregnancyBpDiastolicChange(value: String) {
            onPregnancyBpChange(diastolic = value)
        }

        private fun onPregnancyBpChange(
            systolic: String? = null,
            diastolic: String? = null,
        ) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                val state = _uiState.value
                val sys = systolic ?: state.pregnancyBpSystolicInput
                val dia = diastolic ?: state.pregnancyBpDiastolicInput
                val (bp, error) = LogInputValidators.parseBloodPressure(sys, dia)
                _uiState.update {
                    it.copy(
                        pregnancyBpSystolicInput = sys,
                        pregnancyBpDiastolicInput = dia,
                        pregnancyBpError = error,
                    )
                }
                if (error == null) {
                    persistPregnancyDayLog(
                        date,
                        _uiState.value.copy(
                            pregnancyBpSystolicInput = sys,
                            pregnancyBpDiastolicInput = dia,
                        ),
                    )
                    pulse("pregnancy_bp", LogEditorCategory.PREGNANCY_VITALS)
                    showUndo()
                }
            }
        }

        fun setPain(
            location: PainLocation,
            severity: Int,
        ) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                persistDailyLog(date) { fields ->
                    fields.copy(
                        painLocation = location,
                        painSeverity = severity.coerceIn(0, 10),
                    )
                }
                pulse("pain", LogEditorCategory.PAIN)
                showUndo()
                loadDay(date)
            }
        }

        fun adjustPainSeverity(severity: Int) {
            val date = _uiState.value.date ?: return
            val location = _uiState.value.painLocation ?: PainLocation.ABDOMEN
            setPain(location, severity)
        }

        fun setNote(note: String) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                persistDailyLog(date) { fields -> fields.copy(note = note.takeIf { it.isNotBlank() }) }
                _uiState.update { it.copy(note = note) }
                pulse("note", LogEditorCategory.NOTES)
                showUndo()
            }
        }

        fun setCervicalMucus(type: CervicalMucusType) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                val next = if (_uiState.value.cervicalMucus == type) null else type
                persistTtcLog(date) { it.copy(cervicalMucus = next) }
                pulse("mucus", LogEditorCategory.DISCHARGE)
                showUndo()
                loadDay(date)
            }
        }

        fun setOvulationTest(result: OvulationTestResult) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                val next = if (_uiState.value.ovulationTest == result) null else result
                persistTtcLog(date) { it.copy(ovulationTestResult = next) }
                pulse("opk", LogEditorCategory.TTC_TESTS)
                showUndo()
                loadDay(date)
            }
        }

        fun setPregnancyTest(result: PregnancyTestResult) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                val next = if (_uiState.value.pregnancyTest == result) null else result
                persistTtcLog(date) { it.copy(pregnancyTestResult = next) }
                pulse("hpt", LogEditorCategory.TTC_TESTS)
                showUndo()
                loadDay(date)
            }
        }

        fun toggleIntercourse() {
            if (!_uiState.value.sexualActivityEnabled) return
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                val logged = !_uiState.value.intercourseLogged
                persistTtcLog(date) {
                    it.copy(
                        intercourseLogged = logged,
                        intercourseProtected = if (logged) it.intercourseProtected else null,
                    )
                }
                pulse("intercourse", LogEditorCategory.SEXUAL_ACTIVITY)
                showUndo()
                loadDay(date)
            }
        }

        fun setIntercourseProtected(protected: Boolean?) {
            if (!_uiState.value.sexualActivityEnabled) return
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                persistTtcLog(date) { it.copy(intercourseProtected = protected) }
                showUndo()
                loadDay(date)
            }
        }

        fun toggleLifestyleLogged(category: LogEditorCategory) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                persistDailyLog(date) { fields ->
                    when (category) {
                        LogEditorCategory.SLEEP ->
                            fields.copy(sleepLogged = !fields.sleepLogged)
                        LogEditorCategory.EXERCISE ->
                            fields.copy(exerciseLogged = !fields.exerciseLogged)
                        LogEditorCategory.WATER ->
                            fields.copy(waterGlasses = if (fields.waterGlasses > 0) 0 else 1)
                        LogEditorCategory.STRESS ->
                            fields.copy(stressLevel = if (fields.stressLevel == null) 3 else null)
                        else -> fields
                    }
                }
                pulse("lifestyle_${category.name}", category)
                showUndo()
                loadDay(date)
            }
        }

        fun setCustomSymptomDraft(value: String) {
            _uiState.update { it.copy(customSymptomDraft = value) }
        }

        fun addCustomSymptomFromDraft() {
            val draft = _uiState.value.customSymptomDraft
            addCustomSymptom(draft)
            _uiState.update { it.copy(customSymptomDraft = "") }
        }

        fun togglePregnancySymptom(tag: String) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                val next =
                    if (tag in _uiState.value.pregnancySymptomTags) {
                        _uiState.value.pregnancySymptomTags - tag
                    } else {
                        _uiState.value.pregnancySymptomTags + tag
                    }
                _uiState.update { it.copy(pregnancySymptomTags = next) }
                persistPregnancyDayLog(date, _uiState.value.copy(pregnancySymptomTags = next))
                pulse("pregnancy_symptom", LogEditorCategory.PREGNANCY_SYMPTOMS)
                showUndo()
                loadDay(date)
            }
        }

        fun setMostUsedFirst(enabled: Boolean) {
            viewModelScope.launch {
                loggingPreferencesRepository.setMostUsedFirst(enabled)
            }
        }

        fun disableSexualActivity() {
            viewModelScope.launch {
                loggingPreferencesRepository.setSexualActivityEnabled(false)
            }
        }

        fun moveCategoryEarlier(category: LogEditorCategory) {
            viewModelScope.launch {
                val order = _uiState.value.pinnedCategoryOrder.toMutableList()
                val index = order.indexOf(category)
                if (index > 0) {
                    order.removeAt(index)
                    order.add(index - 1, category)
                    loggingPreferencesRepository.setPinnedCategoryOrder(order)
                } else if (index < 0) {
                    loggingPreferencesRepository.setPinnedCategoryOrder(listOf(category) + order)
                }
            }
        }

        fun moveCategoryLater(category: LogEditorCategory) {
            viewModelScope.launch {
                val order = _uiState.value.pinnedCategoryOrder.toMutableList()
                val index = order.indexOf(category)
                if (index >= 0 && index < order.lastIndex) {
                    order.removeAt(index)
                    order.add(index + 1, category)
                    loggingPreferencesRepository.setPinnedCategoryOrder(order)
                } else if (index < 0) {
                    loggingPreferencesRepository.setPinnedCategoryOrder(order + category)
                }
            }
        }

        fun setPainNote(note: String) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                persistDailyLog(date) { fields -> fields.copy(painNote = note.takeIf { it.isNotBlank() }) }
                showUndo()
                loadDay(date)
            }
        }

        fun clearPain() {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                persistDailyLog(date) { fields ->
                    fields.copy(painLocation = null, painSeverity = null, painNote = null)
                }
                showUndo()
                loadDay(date)
            }
        }

        fun requestSexualOptIn() {
            _uiState.update { it.copy(showSexualOptIn = true) }
        }

        fun confirmSexualOptIn() {
            viewModelScope.launch {
                loggingPreferencesRepository.setSexualActivityEnabled(true)
                _uiState.update { it.copy(showSexualOptIn = false) }
            }
        }

        fun dismissSexualOptIn() {
            _uiState.update { it.copy(showSexualOptIn = false) }
        }

        fun openManageCategories() {
            _uiState.update { it.copy(showManageCategories = true) }
        }

        fun closeManageCategories() {
            _uiState.update { it.copy(showManageCategories = false) }
        }

        fun setLifestyleEnabled(
            category: LogEditorCategory,
            enabled: Boolean,
        ) {
            viewModelScope.launch {
                loggingPreferencesRepository.setLifestyleModuleEnabled(category, enabled)
            }
        }

        fun addCustomSymptom(label: String) {
            val trimmed = label.trim()
            if (trimmed.isEmpty()) return
            val date = _uiState.value.date
            viewModelScope.launch {
                customSymptomTagRepository.upsert(
                    CustomSymptomTagEntity(
                        id = UUID.randomUUID().toString(),
                        label = trimmed,
                        createdAtEpochMs = System.currentTimeMillis(),
                    ),
                )
                if (date != null) {
                    captureUndo()
                    persistDailyLog(date) { fields ->
                        fields.copy(localCustomSymptomTags = fields.localCustomSymptomTags + trimmed)
                    }
                    showUndo()
                    loadDay(date)
                }
            }
        }

        private suspend fun loadDay(date: LocalDate) {
            val periodDay = cycleRepository.getPeriodDayForDate(date)
            val daily = cycleRepository.getDailyLogForDate(date)
            val ttc = ttcRepository.getTtcLogForDate(date)
            val pregnancyDay =
                if (_uiState.value.reproductiveMode == ReproductiveMode.PREGNANT) {
                    pregnancyRepository.getPregnancyDayLogForDate(date)
                } else {
                    null
                }
            val fields = DailyLogFieldSupport.fromEntity(daily)
            val pregnancySymptoms =
                pregnancyDay
                    ?.symptomTags
                    ?.let(DailyLogFieldSupport::parseTags) ?: emptySet()
            val weightInput =
                pregnancyDay?.weightKg?.let { PregnancyFormatters.formatDecimal(it) } ?: ""
            val bpSys = pregnancyDay?.bpSystolic?.toString() ?: ""
            val bpDia = pregnancyDay?.bpDiastolic?.toString() ?: ""
            val usageCounts = computeCategoryUsageCounts(date)
            val bbtInput = ttc?.bbtCelsius?.toString() ?: ""
            _uiState.update { state ->
                val withFields =
                    state.copy(
                        loading = false,
                        periodLogged = periodDay != null,
                        flowLevel = periodDay?.flowLevel,
                        symptomTags = fields.symptomTags,
                        localCustomSymptomTags = fields.localCustomSymptomTags,
                        moodTags = fields.moodTags,
                        medicationTags = fields.medicationTags,
                        painLocation = fields.painLocation,
                        painSeverity = fields.painSeverity,
                        painNote = fields.painNote ?: "",
                        note = fields.note ?: "",
                        sleepLogged = fields.sleepLogged,
                        exerciseLogged = fields.exerciseLogged,
                        waterGlasses = fields.waterGlasses,
                        stressLevel = fields.stressLevel,
                        cervicalMucus = ttc?.cervicalMucus,
                        ovulationTest = ttc?.ovulationTestResult,
                        pregnancyTest = ttc?.pregnancyTestResult,
                        intercourseLogged = ttc?.intercourseLogged == true,
                        intercourseProtected = ttc?.intercourseProtected,
                        bbtInput = bbtInput,
                        bbtError = null,
                        pregnancySymptomTags = pregnancySymptoms,
                        pregnancyWeightInput = weightInput,
                        pregnancyBpSystolicInput = bpSys,
                        pregnancyBpDiastolicInput = bpDia,
                        pregnancyWeightError = null,
                        pregnancyBpError = null,
                        categoryUsageCounts = usageCounts,
                    )
                withFields.copy(
                    orderedCategories =
                        buildCategoryOrder(
                            state = withFields,
                            pinned = withFields.pinnedCategoryOrder,
                            sexualEnabled = withFields.sexualActivityEnabled,
                            mode = withFields.reproductiveMode,
                            mostUsedFirst = withFields.mostUsedFirst,
                            referenceDate = date,
                            usageCounts = usageCounts,
                        ),
                )
            }
        }

        private suspend fun computeCategoryUsageCounts(referenceDate: LocalDate): Map<LogEditorCategory, Int> {
            val windowStart = referenceDate.minusDays(LogCategoryUsageFromLocalLogs.WINDOW_DAYS)
            val daily =
                cycleRepository.observeDailyLogs(windowStart, referenceDate).first().map { entity ->
                    val parsed = DailyLogFieldSupport.fromEntity(entity)
                    LogCategoryUsageFromLocalLogs.DailyLogUsageRow(
                        logDate = entity.logDate,
                        moodTags = parsed.moodTags,
                        symptomTags = parsed.symptomTags,
                        medicationTags = parsed.medicationTags,
                        localCustomSymptomTags = parsed.localCustomSymptomTags,
                        painSeverity = parsed.painSeverity,
                        note = parsed.note,
                        sleepLogged = parsed.sleepLogged,
                        exerciseLogged = parsed.exerciseLogged,
                        waterGlasses = parsed.waterGlasses,
                        stressLevel = parsed.stressLevel,
                    )
                }
            val ttc =
                ttcRepository.getTtcLogsInRange(windowStart, referenceDate).map { row ->
                    LogCategoryUsageFromLocalLogs.TtcLogUsageRow(
                        logDate = row.logDate,
                        bbtLogged = row.bbtCelsius != null,
                        cervicalMucusLogged = row.cervicalMucus != null,
                        ovulationTestLogged = row.ovulationTestResult != null,
                        pregnancyTestLogged = row.pregnancyTestResult != null,
                        intercourseLogged = row.intercourseLogged,
                    )
                }
            val periodDays =
                cycleRepository.getPeriodDaysInRange(windowStart, referenceDate).map { row ->
                    LogCategoryUsageFromLocalLogs.PeriodDayUsageRow(
                        logDate = row.logDate,
                        flowLogged = row.flowLevel != null,
                    )
                }
            return LogCategoryUsageFromLocalLogs.countUsage(
                referenceDate = referenceDate,
                dailyLogs = daily,
                ttcLogs = ttc,
                periodDays = periodDays,
            )
        }

        private suspend fun ensurePeriodSpan(date: LocalDate) {
            val existing =
                cycleRepository.getAllPeriods().find { record ->
                    !date.isBefore(record.startDate) &&
                        (record.endDate == null || !date.isAfter(record.endDate))
                }
            if (existing == null) {
                cycleRepository.upsertPeriod(
                    startDate = date,
                    endDate = date,
                    note = null,
                    recordId = null,
                )
            }
        }

        private fun captureUndo() {
            val state = _uiState.value
            lastUndo =
                UndoSnapshot(
                    periodLogged = state.periodLogged,
                    flowLevel = state.flowLevel,
                    symptomTags = state.symptomTags,
                    localCustomSymptomTags = state.localCustomSymptomTags,
                    moodTags = state.moodTags,
                    medicationTags = state.medicationTags,
                    painLocation = state.painLocation,
                    painSeverity = state.painSeverity,
                    painNote = state.painNote.takeIf { it.isNotBlank() },
                    note = state.note.takeIf { it.isNotBlank() },
                    sleepLogged = state.sleepLogged,
                    exerciseLogged = state.exerciseLogged,
                    waterGlasses = state.waterGlasses,
                    stressLevel = state.stressLevel,
                    bbtInput = state.bbtInput,
                    cervicalMucus = state.cervicalMucus,
                    ovulationTest = state.ovulationTest,
                    pregnancyTest = state.pregnancyTest,
                    intercourseLogged = state.intercourseLogged,
                    intercourseProtected = state.intercourseProtected,
                    pregnancySymptomTags = state.pregnancySymptomTags,
                    pregnancyWeightInput = state.pregnancyWeightInput,
                    pregnancyBpSystolicInput = state.pregnancyBpSystolicInput,
                    pregnancyBpDiastolicInput = state.pregnancyBpDiastolicInput,
                )
        }

        private suspend fun applySnapshot(
            date: LocalDate,
            snapshot: UndoSnapshot,
        ) {
            val current = _uiState.value
            if (snapshot.periodLogged != current.periodLogged || snapshot.flowLevel != current.flowLevel) {
                if (snapshot.periodLogged) {
                    periodDayTrackingService.applyUserPeriodLogChange(
                        healthConnectIntegrationActive = healthConnectPeriodDayIntegrationGate.isIntegrationActive(),
                        loggingPeriod = true,
                        date = date,
                        flowLevel = snapshot.flowLevel,
                        ensurePeriodSpanExists = { ensurePeriodSpan(date) },
                    )
                } else {
                    periodDayTrackingService.applyUserPeriodLogChange(
                        healthConnectIntegrationActive = healthConnectPeriodDayIntegrationGate.isIntegrationActive(),
                        loggingPeriod = false,
                        date = date,
                        flowLevel = null,
                        ensurePeriodSpanExists = {},
                    )
                }
            }
            persistDailyLog(date) { fields ->
                fields.copy(
                    moodTags = snapshot.moodTags,
                    symptomTags = snapshot.symptomTags,
                    localCustomSymptomTags = snapshot.localCustomSymptomTags,
                    medicationTags = snapshot.medicationTags,
                    painSeverity = snapshot.painSeverity,
                    painLocation = snapshot.painLocation,
                    painNote = snapshot.painNote,
                    note = snapshot.note,
                    sleepLogged = snapshot.sleepLogged,
                    exerciseLogged = snapshot.exerciseLogged,
                    waterGlasses = snapshot.waterGlasses,
                    stressLevel = snapshot.stressLevel,
                )
            }
            val (bbtCelsius, _) = LogInputValidators.parseBbt(snapshot.bbtInput)
            persistTtcLog(date) {
                it.copy(
                    bbtCelsius = bbtCelsius,
                    cervicalMucus = snapshot.cervicalMucus,
                    ovulationTestResult = snapshot.ovulationTest,
                    pregnancyTestResult = snapshot.pregnancyTest,
                    intercourseLogged = snapshot.intercourseLogged,
                    intercourseProtected = snapshot.intercourseProtected,
                )
            }
            if (_uiState.value.reproductiveMode == ReproductiveMode.PREGNANT) {
                val restored =
                    _uiState.value.copy(
                        pregnancySymptomTags = snapshot.pregnancySymptomTags,
                        pregnancyWeightInput = snapshot.pregnancyWeightInput,
                        pregnancyBpSystolicInput = snapshot.pregnancyBpSystolicInput,
                        pregnancyBpDiastolicInput = snapshot.pregnancyBpDiastolicInput,
                    )
                persistPregnancyDayLog(date, restored)
            }
        }

        private fun showUndo() {
            _uiState.update { it.copy(undoMessage = "undo") }
        }

        private fun pulse(
            key: String,
            @Suppress("UnusedParameter") category: LogEditorCategory? = null,
        ) {
            _uiState.update { state ->
                state.copy(confirmPulseKey = key)
            }
        }

        private suspend fun persistTtcLog(
            date: LocalDate,
            transform: (TtcDayLogInput) -> TtcDayLogInput,
        ) {
            saveMutex.withLock {
                val existing = ttcRepository.getTtcLogForDate(date)
                val base =
                    TtcDayLogInput(
                        logDate = date,
                        bbtCelsius = existing?.bbtCelsius,
                        ovulationTestResult = existing?.ovulationTestResult,
                        cervicalMucus = existing?.cervicalMucus,
                        intercourseLogged = existing?.intercourseLogged == true,
                        intercourseProtected = existing?.intercourseProtected,
                        pregnancyTestResult = existing?.pregnancyTestResult,
                    )
                ttcRepository.upsertTtcDayLog(transform(base))
            }
        }

        @Suppress("CyclomaticComplexMethod")
        private fun buildCategoryOrder(
            state: DayEditorUiState,
            pinned: List<LogEditorCategory>,
            sexualEnabled: Boolean,
            mode: ReproductiveMode,
            mostUsedFirst: Boolean,
            referenceDate: LocalDate,
            usageCounts: Map<LogEditorCategory, Int>,
        ): List<LogEditorCategory> {
            if (referenceDate != localDateClock.today()) {
                // Category order is global; reference date anchors future per-day ordering rules.
            }
            val base =
                when (mode) {
                    ReproductiveMode.PREGNANT ->
                        buildList {
                            add(LogEditorCategory.PREGNANCY_SYMPTOMS)
                            add(LogEditorCategory.PREGNANCY_VITALS)
                            add(LogEditorCategory.MEDICATION)
                            add(LogEditorCategory.NOTES)
                            add(LogEditorCategory.MOOD)
                            if (sexualEnabled) add(LogEditorCategory.SEXUAL_ACTIVITY)
                            if (state.lifestyleEnabled[LogEditorCategory.SLEEP] == true) add(LogEditorCategory.SLEEP)
                            if (state.lifestyleEnabled[LogEditorCategory.EXERCISE] == true) {
                                add(LogEditorCategory.EXERCISE)
                            }
                            if (state.lifestyleEnabled[LogEditorCategory.WATER] == true) add(LogEditorCategory.WATER)
                            if (state.lifestyleEnabled[LogEditorCategory.STRESS] == true) add(LogEditorCategory.STRESS)
                        }
                    else ->
                        buildList {
                            add(LogEditorCategory.PERIOD_FLOW)
                            add(LogEditorCategory.SYMPTOMS)
                            add(LogEditorCategory.MOOD)
                            add(LogEditorCategory.PAIN)
                            add(LogEditorCategory.DISCHARGE)
                            if (mode == ReproductiveMode.TRYING_TO_CONCEIVE) {
                                add(LogEditorCategory.TTC_TESTS)
                            }
                            add(LogEditorCategory.MEDICATION)
                            add(LogEditorCategory.NOTES)
                            if (sexualEnabled) add(LogEditorCategory.SEXUAL_ACTIVITY)
                            if (state.lifestyleEnabled[LogEditorCategory.SLEEP] == true) add(LogEditorCategory.SLEEP)
                            if (state.lifestyleEnabled[LogEditorCategory.EXERCISE] == true) {
                                add(LogEditorCategory.EXERCISE)
                            }
                            if (state.lifestyleEnabled[LogEditorCategory.WATER] == true) add(LogEditorCategory.WATER)
                            if (state.lifestyleEnabled[LogEditorCategory.STRESS] == true) add(LogEditorCategory.STRESS)
                        }
                }
            return LogCategoryUsageOrder.orderedCategories(
                enabled = base.toSet(),
                pinnedOrder = pinned,
                usageCounts = usageCounts,
                mostUsedFirst = mostUsedFirst,
            )
        }

        private suspend fun persistDailyLog(
            date: LocalDate,
            transform: (DailyLogFields) -> DailyLogFields,
        ) {
            saveMutex.withLock {
                val current = DailyLogFieldSupport.fromEntity(cycleRepository.getDailyLogForDate(date))
                val next = transform(current)
                cycleRepository.upsertDailyLog(
                    date = date,
                    moodTags = next.moodTags,
                    symptomTags = next.symptomTags,
                    painSeverity = next.painSeverity,
                    painLocation = next.painLocation?.name,
                    painNote = next.painNote,
                    note = next.note,
                    medicationTags = next.medicationTags,
                    localCustomSymptomTags = next.localCustomSymptomTags,
                    sleepLogged = next.sleepLogged,
                    exerciseLogged = next.exerciseLogged,
                    waterGlasses = next.waterGlasses,
                    stressLevel = next.stressLevel,
                )
            }
        }

        private suspend fun persistPregnancyDayLog(
            date: LocalDate,
            state: DayEditorUiState,
        ) {
            val (weightKg, weightError) = LogInputValidators.parseWeight(state.pregnancyWeightInput)
            if (weightError != null) return
            val (bp, bpError) =
                LogInputValidators.parseBloodPressure(
                    state.pregnancyBpSystolicInput,
                    state.pregnancyBpDiastolicInput,
                )
            if (bpError != null) return
            pregnancyRepository.upsertPregnancyDayLog(
                PregnancyDayLogInput(
                    logDate = date,
                    symptomTags = state.pregnancySymptomTags,
                    weightKg = weightKg,
                    bpSystolic = bp?.first,
                    bpDiastolic = bp?.second,
                    note = state.note.takeIf { it.isNotBlank() },
                ),
            )
        }
    }
