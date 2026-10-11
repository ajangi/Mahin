package dev.mahin.android.cycle.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val cervicalMucus: CervicalMucusType? = null,
    val ovulationTest: OvulationTestResult? = null,
    val pregnancyTest: PregnancyTestResult? = null,
    val intercourseLogged: Boolean = false,
    val intercourseProtected: Boolean? = null,
    val customSymptoms: List<String> = emptyList(),
    val availableSymptoms: List<String> = LogDayEditorCatalog.defaultSymptoms,
    val availableMoods: List<String> = LogDayEditorCatalog.defaultMoods,
    val undoMessage: String? = null,
    val confirmPulseKey: String? = null,
)

data class UndoSnapshot(
    val periodLogged: Boolean,
    val flowLevel: PeriodFlowLevel?,
    val symptomTags: Set<String>,
    val moodTags: Set<String>,
    val painLocation: PainLocation?,
    val painSeverity: Int?,
)

@HiltViewModel
@Suppress("LongParameterList", "TooManyFunctions")
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

        init {
            viewModelScope.launch {
                combine(
                    loggingPreferencesRepository.observeSexualActivityEnabled(),
                    loggingPreferencesRepository.observeMostUsedFirst(),
                    loggingPreferencesRepository.observePinnedCategoryOrder(),
                    ttcRepository.observeProfile(),
                ) { sexual, _, pinned, profile ->
                    val mode = profile?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING
                    Triple(sexual, pinned, mode)
                }.collect { (sexual, pinned, mode) ->
                    _uiState.update { state ->
                        state.copy(
                            sexualActivityEnabled = sexual,
                            reproductiveMode = mode,
                            orderedCategories = buildCategoryOrder(state, pinned, sexual, mode),
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
                pulse("period")
                showUndo()
                loadDay(date)
            }
        }

        fun toggleSymptom(tag: String) {
            mutateDailyLog { symptoms, moods, painLoc, painSev ->
                val next = if (tag in symptoms) symptoms - tag else symptoms + tag
                cycleRepository.upsertDailyLog(
                    date = _uiState.value.date!!,
                    moodTags = moods,
                    symptomTags = next,
                    painSeverity = painSev,
                    painLocation = painLoc?.name,
                    painNote = null,
                    note = null,
                )
                pulse("symptom_$tag")
            }
        }

        fun toggleMood(tag: String) {
            mutateDailyLog { symptoms, moods, painLoc, painSev ->
                val next = if (tag in moods) moods - tag else moods + tag
                cycleRepository.upsertDailyLog(
                    date = _uiState.value.date!!,
                    moodTags = next,
                    symptomTags = symptoms,
                    painSeverity = painSev,
                    painLocation = painLoc?.name,
                    painNote = null,
                    note = null,
                )
                pulse("mood_$tag")
            }
        }

        fun setPain(
            location: PainLocation,
            severity: Int,
        ) {
            mutateDailyLog { symptoms, moods, _, _ ->
                cycleRepository.upsertDailyLog(
                    date = _uiState.value.date!!,
                    moodTags = moods,
                    symptomTags = symptoms,
                    painSeverity = severity.coerceIn(0, 10),
                    painLocation = location.name,
                    painNote = null,
                    note = null,
                )
                pulse("pain")
            }
        }

        fun clearPain() {
            mutateDailyLog { symptoms, moods, _, _ ->
                cycleRepository.upsertDailyLog(
                    date = _uiState.value.date!!,
                    moodTags = moods,
                    symptomTags = symptoms,
                    painSeverity = null,
                    painLocation = null,
                    painNote = null,
                    note = null,
                )
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

        private fun mutateDailyLog(block: suspend (Set<String>, Set<String>, PainLocation?, Int?) -> Unit) {
            val date = _uiState.value.date ?: return
            captureUndo()
            viewModelScope.launch {
                val daily = cycleRepository.getDailyLogForDate(date)
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
                val painLoc = daily?.painLocation?.let { runCatching { PainLocation.valueOf(it) }.getOrNull() }
                val painSev = daily?.painSeverity
                block(symptoms, moods, painLoc, painSev)
                showUndo()
                loadDay(date)
            }
        }

        private suspend fun loadDay(date: LocalDate) {
            val periodDay = cycleRepository.getPeriodDayForDate(date)
            val daily = cycleRepository.getDailyLogForDate(date)
            val ttc = ttcRepository.getTtcLogForDate(date)
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
            _uiState.update { state ->
                state.copy(
                    loading = false,
                    periodLogged = periodDay != null,
                    flowLevel = periodDay?.flowLevel,
                    symptomTags = symptoms,
                    moodTags = moods,
                    painLocation = daily?.painLocation?.let { runCatching { PainLocation.valueOf(it) }.getOrNull() },
                    painSeverity = daily?.painSeverity,
                    cervicalMucus = ttc?.cervicalMucus,
                    ovulationTest = ttc?.ovulationTestResult,
                    pregnancyTest = ttc?.pregnancyTestResult,
                    intercourseLogged = ttc?.intercourseLogged == true,
                    intercourseProtected = ttc?.intercourseProtected,
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
            cycleRepository.upsertDailyLog(
                date = date,
                moodTags = snapshot.moodTags,
                symptomTags = snapshot.symptomTags,
                painSeverity = snapshot.painSeverity,
                painLocation = snapshot.painLocation?.name,
                painNote = null,
                note = null,
            )
        }

        private fun showUndo() {
            _uiState.update { it.copy(undoMessage = "undo") }
        }

        private fun pulse(key: String) {
            _uiState.update { it.copy(confirmPulseKey = key) }
        }

        private fun buildCategoryOrder(
            state: DayEditorUiState,
            pinned: List<LogEditorCategory>,
            sexualEnabled: Boolean,
            mode: ReproductiveMode,
        ): List<LogEditorCategory> {
            val base =
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
                    if (state.lifestyleEnabled[LogEditorCategory.EXERCISE] == true) add(LogEditorCategory.EXERCISE)
                    if (state.lifestyleEnabled[LogEditorCategory.WATER] == true) add(LogEditorCategory.WATER)
                    if (state.lifestyleEnabled[LogEditorCategory.STRESS] == true) add(LogEditorCategory.STRESS)
                    if (mode == ReproductiveMode.PREGNANT) {
                        add(LogEditorCategory.PREGNANCY_SYMPTOMS)
                        add(LogEditorCategory.PREGNANCY_VITALS)
                    }
                }
            return LogCategoryUsageOrder.orderedCategories(
                enabled = base.toSet(),
                pinnedOrder = pinned,
                usageCounts = emptyMap(),
                mostUsedFirst = false,
            )
        }

    }
