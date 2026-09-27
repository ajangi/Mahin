package dev.mahin.android.pregnancy

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.database.entity.PregnancyAppointmentEntity
import dev.mahin.core.database.entity.PregnancyRecordEntity
import dev.mahin.core.database.pregnancy.PregnancyAppointmentInput
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.ContractionTimerSnapshot
import dev.mahin.core.datastore.KickTimerSnapshot
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.model.PregnancyAppointmentType
import dev.mahin.core.model.PregnancyOutcome
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.pregnancy.PregnancyDatingEngineV1
import dev.mahin.domain.pregnancy.PregnancyStatusSnapshot
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PregnancyHubUiState(
    val isLoading: Boolean = true,
    val isPregnantMode: Boolean = false,
    val postTransition: Boolean = false,
    val activePregnancy: PregnancyRecordEntity? = null,
    val status: PregnancyStatusSnapshot? = null,
    val kickSessionId: String? = null,
    val kickCount: Int = 0,
    val kickElapsedSeconds: Long = 0L,
    val contractionSessionId: String? = null,
    val openContractionEventId: String? = null,
    val contractionElapsedSeconds: Long = 0L,
    val appointments: List<PregnancyAppointmentEntity> = emptyList(),
    val newAppointmentTitle: String = "",
    val newAppointmentType: PregnancyAppointmentType = PregnancyAppointmentType.CLINICIAN_VISIT,
    val newAppointmentJalali: JalaliDate = PersianCivilDateConverter.toJalali(LocalDate.now()),
    val selectedOutcome: PregnancyOutcome? = null,
    val wantsSupportContent: Boolean = false,
    val suppressCelebratoryNotifications: Boolean = false,
    val weeklyCmsTitle: String? = null,
    val weeklyCmsSummary: String? = null,
)

@HiltViewModel
class PregnancyHubViewModel
    @Inject
    constructor(
        private val repository: PregnancyTrackingRepository,
        private val timerPreferences: PregnancyTimerPreferencesRepository,
        private val contentRepository: ContentRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private var lastFetchedWeek: Int? = null
        private val _uiState = MutableStateFlow(PregnancyHubUiState())
        val uiState: StateFlow<PregnancyHubUiState> = _uiState.asStateFlow()

        private var tickerJob: Job? = null

        init {
            savedStateHandle.get<String>("restored")?.let { /* touch SavedStateHandle for config change */ }
            viewModelScope.launch {
                combine(
                    repository.observeProfile(),
                    repository.observeActivePregnancy(),
                    timerPreferences.observeKickTimer(),
                    timerPreferences.observeContractionTimer(),
                ) { profile, pregnancy, kickTimer, contractionTimer ->
                    HubInputs(profile?.reproductiveMode, pregnancy, kickTimer, contractionTimer)
                }.flatMapLatest { inputs ->
                    val appointmentFlow =
                        inputs.pregnancy?.let { repository.observeAppointments(it.id) } ?: flowOf(emptyList())
                    appointmentFlow.map { appointments -> inputs to appointments }
                }.collect { (inputs, appointments) ->
                    val mode = inputs.mode
                    val pregnancy = inputs.pregnancy
                    val kickTimer = inputs.kickTimer
                    val contractionTimer = inputs.contractionTimer
                    val pregnantMode = mode == ReproductiveMode.PREGNANT
                    val postTransition = mode == ReproductiveMode.POST_PREGNANCY_TRANSITION
                    val status =
                        pregnancy?.let {
                            PregnancyDatingEngineV1.status(
                                lmpDate = it.lmpDate,
                                clinicalEddDate = it.clinicalEddDate,
                                asOfDate = LocalDate.now(),
                            )
                        }
                    val kickSessionId =
                        repository.validateKickTimerSession(kickTimer.sessionId, pregnancy?.id)
                    val (contractionSessionId, openContractionEventId) =
                        repository.validateContractionTimerSession(
                            sessionId = contractionTimer.sessionId,
                            openEventId = contractionTimer.openEventId,
                            activePregnancyId = pregnancy?.id,
                        )
                    val suppressCelebratory = repository.shouldSuppressCelebratoryNotifications()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPregnantMode = pregnantMode,
                            postTransition = postTransition,
                            activePregnancy = pregnancy,
                            status = status,
                            kickSessionId = kickSessionId,
                            contractionSessionId = contractionSessionId,
                            openContractionEventId = openContractionEventId,
                            appointments = appointments,
                            suppressCelebratoryNotifications = suppressCelebratory,
                        )
                    }
                    status?.displayWeekNumber?.let { week ->
                        if (week != lastFetchedWeek) {
                            lastFetchedWeek = week
                            viewModelScope.launch {
                                val article = contentRepository.pregnancyWeek(week)
                                _uiState.update {
                                    it.copy(
                                        weeklyCmsTitle = article?.title,
                                        weeklyCmsSummary = article?.summary,
                                    )
                                }
                            }
                        }
                    }
                    viewModelScope.launch { refreshKickCount(kickSessionId) }
                    restartTicker(kickTimer, contractionTimer)
                }
            }
        }

        fun onNewAppointmentTitleChange(value: String) {
            _uiState.update { it.copy(newAppointmentTitle = value) }
        }

        fun onNewAppointmentTypeSelected(type: PregnancyAppointmentType) {
            _uiState.update { it.copy(newAppointmentType = type) }
        }

        fun onNewAppointmentDateSelected(jalali: JalaliDate) {
            _uiState.update { it.copy(newAppointmentJalali = jalali) }
        }

        fun addAppointment() {
            viewModelScope.launch {
                val state = _uiState.value
                val pregnancy = state.activePregnancy ?: return@launch
                val title = state.newAppointmentTitle.trim()
                if (title.isEmpty()) return@launch
                val date = PersianCivilDateConverter.toGregorian(state.newAppointmentJalali)
                val epochMs = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                repository.upsertAppointment(
                    PregnancyAppointmentInput(
                        id = null,
                        pregnancyId = pregnancy.id,
                        appointmentType = state.newAppointmentType,
                        title = title,
                        scheduledAtEpochMs = epochMs,
                        location = null,
                        clinicianName = null,
                        note = null,
                        reminderEnabled = false,
                    ),
                )
                _uiState.update { it.copy(newAppointmentTitle = "") }
            }
        }

        fun startKickSession() {
            viewModelScope.launch {
                val pregnancy = _uiState.value.activePregnancy ?: return@launch
                val session = repository.startKickSession(pregnancy.id)
                timerPreferences.setActiveKickSession(session.id, session.startedAtEpochMs)
                _uiState.update { it.copy(kickSessionId = session.id, kickCount = 0) }
            }
        }

        fun stopKickSession() {
            viewModelScope.launch {
                val sessionId = _uiState.value.kickSessionId ?: return@launch
                repository.endKickSession(sessionId)
                timerPreferences.setActiveKickSession(null, null)
                _uiState.update { it.copy(kickSessionId = null, kickCount = 0, kickElapsedSeconds = 0L) }
            }
        }

        fun recordKick() {
            viewModelScope.launch {
                val sessionId = _uiState.value.kickSessionId ?: return@launch
                repository.recordKick(sessionId)
                refreshKickCount(sessionId)
            }
        }

        fun startContractionSession() {
            viewModelScope.launch {
                val pregnancy = _uiState.value.activePregnancy ?: return@launch
                val session = repository.startContractionSession(pregnancy.id)
                timerPreferences.setActiveContractionTimer(session.id, null, null)
            }
        }

        fun endContractionSession() {
            viewModelScope.launch {
                val sessionId = _uiState.value.contractionSessionId ?: return@launch
                repository.endContractionSession(sessionId)
                timerPreferences.setActiveContractionTimer(null, null, null)
            }
        }

        fun toggleContraction() {
            viewModelScope.launch {
                val sessionId = _uiState.value.contractionSessionId ?: return@launch
                val openEventId = _uiState.value.openContractionEventId
                if (openEventId == null) {
                    val event = repository.startContraction(sessionId)
                    timerPreferences.setActiveContractionTimer(
                        sessionId,
                        event.id,
                        event.startedAtEpochMs,
                    )
                } else {
                    repository.stopContraction(openEventId)
                    timerPreferences.setActiveContractionTimer(sessionId, null, null)
                }
            }
        }

        fun onOutcomeSelected(outcome: PregnancyOutcome) {
            _uiState.update { it.copy(selectedOutcome = outcome) }
        }

        fun onSupportContentToggle(enabled: Boolean) {
            _uiState.update { it.copy(wantsSupportContent = enabled) }
        }

        fun saveOutcome() {
            viewModelScope.launch {
                val state = _uiState.value
                val pregnancy = state.activePregnancy ?: return@launch
                val outcome = state.selectedOutcome ?: return@launch
                repository.recordOutcome(
                    pregnancyId = pregnancy.id,
                    outcome = outcome,
                    wantsSupportContent = if (state.wantsSupportContent) true else null,
                )
                _uiState.update { it.copy(selectedOutcome = null, wantsSupportContent = false) }
            }
        }

        fun resumeCycleTracking() {
            viewModelScope.launch {
                repository.resumeTracking(ReproductiveMode.CYCLE_TRACKING)
            }
        }

        fun resumeTtc() {
            viewModelScope.launch {
                repository.resumeTracking(ReproductiveMode.TRYING_TO_CONCEIVE)
            }
        }

        private suspend fun refreshKickCount(sessionId: String?) {
            if (sessionId == null) {
                _uiState.update { it.copy(kickCount = 0) }
                return
            }
            val count = repository.kickCount(sessionId)
            _uiState.update { it.copy(kickCount = count) }
        }

        private fun restartTicker(
            kickTimer: KickTimerSnapshot,
            contractionTimer: ContractionTimerSnapshot,
        ) {
            tickerJob?.cancel()
            tickerJob =
                viewModelScope.launch {
                    while (isActive) {
                        val now = System.currentTimeMillis()
                        val kickElapsed =
                            kickTimer.startedAtEpochMs?.let { start ->
                                ((now - start) / 1000L).coerceAtLeast(0L)
                            } ?: 0L
                        val contractionElapsed =
                            contractionTimer.openEventStartedEpochMs?.let { start ->
                                ((now - start) / 1000L).coerceAtLeast(0L)
                            } ?: 0L
                        _uiState.update {
                            it.copy(
                                kickElapsedSeconds = kickElapsed,
                                contractionElapsedSeconds = contractionElapsed,
                            )
                        }
                        delay(1_000L)
                    }
                }
        }

        private data class HubInputs(
            val mode: ReproductiveMode?,
            val pregnancy: PregnancyRecordEntity?,
            val kickTimer: KickTimerSnapshot,
            val contractionTimer: ContractionTimerSnapshot,
        )
    }
