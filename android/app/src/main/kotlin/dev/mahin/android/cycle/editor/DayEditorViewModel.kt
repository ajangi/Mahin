package dev.mahin.android.cycle.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.common.LocalDateClock
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.CustomSymptomTagEntity
import dev.mahin.core.database.logging.CustomSymptomTagRepository
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.database.pregnancy.PregnancyDayLogInput
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
import dev.mahin.domain.cycle.LogCategoryUsageOrder
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    val moodTags: Set<String>,
    val painLocation: PainLocation?,
    val painSeverity: Int?,
    val painNote: String?,
    val note: String?,
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
                            availableSymptoms = LogDayEditorCatalog.defaultSymptoms + labels,
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

        fun togglePeriodLogged() {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                val state = _uiState.value
                val next = !state.periodLogged
                if (next) {
                    periodDayTrackingService.applyUserPeriodLogChange(
                        healthConnectIntegrationActive = healthConnectPeriodDayIntegrationGate.isIntegrationActive(),
                        loggingPeriod = true,
                        date = date,
                        flowLevel = state.flowLevel ?: PeriodFlowLevel.MEDIUM,
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
            _uiState.update { state ->
                when (category) {
                    LogEditorCategory.SLEEP ->
                        state.copy(sleepLogged = !state.sleepLogged)
                    LogEditorCategory.EXERCISE ->
                        state.copy(exerciseLogged = !state.exerciseLogged)
                    LogEditorCategory.WATER ->
                        state.copy(waterGlasses = if (state.waterGlasses > 0) 0 else 1)
                    LogEditorCategory.STRESS ->
                        state.copy(stressLevel = if (state.stressLevel == null) 3 else null)
                    else -> state
                }
            }
            pulse("lifestyle_${category.name}", category)
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
                pregnancyRepository.upsertPregnancyDayLog(
                    PregnancyDayLogInput(
                        logDate = date,
                        symptomTags = next,
                        weightKg = null,
                        bpSystolic = null,
                        bpDiastolic = null,
                        note = _uiState.value.note.takeIf { it.isNotBlank() },
                    ),
                )
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
            viewModelScope.launch {
                customSymptomTagRepository.upsert(
                    CustomSymptomTagEntity(
                        id = UUID.randomUUID().toString(),
                        label = trimmed,
                        createdAtEpochMs = System.currentTimeMillis(),
                    ),
                )
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
            _uiState.update { state ->
                state.copy(
                    loading = false,
                    periodLogged = periodDay != null,
                    flowLevel = periodDay?.flowLevel,
                    symptomTags = fields.symptomTags,
                    moodTags = fields.moodTags,
                    painLocation = fields.painLocation,
                    painSeverity = fields.painSeverity,
                    painNote = fields.painNote ?: "",
                    note = fields.note ?: "",
                    cervicalMucus = ttc?.cervicalMucus,
                    ovulationTest = ttc?.ovulationTestResult,
                    pregnancyTest = ttc?.pregnancyTestResult,
                    intercourseLogged = ttc?.intercourseLogged == true,
                    intercourseProtected = ttc?.intercourseProtected,
                    pregnancySymptomTags = pregnancySymptoms,
                )
            }
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
                    moodTags = state.moodTags,
                    painLocation = state.painLocation,
                    painSeverity = state.painSeverity,
                    painNote = state.painNote.takeIf { it.isNotBlank() },
                    note = state.note.takeIf { it.isNotBlank() },
                )
        }

        private suspend fun applySnapshot(
            date: LocalDate,
            snapshot: UndoSnapshot,
        ) {
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
            persistDailyLog(date) { fields ->
                fields.copy(
                    moodTags = snapshot.moodTags,
                    symptomTags = snapshot.symptomTags,
                    painSeverity = snapshot.painSeverity,
                    painLocation = snapshot.painLocation,
                    painNote = snapshot.painNote,
                    note = snapshot.note,
                )
            }
        }

        private fun showUndo() {
            _uiState.update { it.copy(undoMessage = "undo") }
        }

        private fun pulse(
            key: String,
            category: LogEditorCategory? = null,
        ) {
            _uiState.update { state ->
                val counts =
                    if (category != null) {
                        val current = state.categoryUsageCounts[category] ?: 0
                        state.categoryUsageCounts + (category to current + 1)
                    } else {
                        state.categoryUsageCounts
                    }
                state.copy(
                    confirmPulseKey = key,
                    categoryUsageCounts = counts,
                    orderedCategories =
                        buildCategoryOrder(
                            state = state,
                            pinned = state.pinnedCategoryOrder,
                            sexualEnabled = state.sexualActivityEnabled,
                            mode = state.reproductiveMode,
                            mostUsedFirst = state.mostUsedFirst,
                            referenceDate = state.date ?: localDateClock.today(),
                            usageCounts = counts,
                        ),
                )
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
                )
            }
        }
    }
