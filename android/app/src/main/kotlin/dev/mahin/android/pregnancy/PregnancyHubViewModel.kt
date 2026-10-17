package dev.mahin.android.pregnancy

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.common.InstantClock
import dev.mahin.core.common.LocalDateClock
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.database.entity.PregnancyRecordEntity
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.ContractionTimerSnapshot
import dev.mahin.core.datastore.KickTimerSnapshot
import dev.mahin.core.datastore.PregnancyTimerPreferencesRepository
import dev.mahin.core.model.PregnancyOutcome
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.pregnancy.PregnancyDatingEngineV1
import dev.mahin.domain.pregnancy.PregnancyStatusSnapshot
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.first
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
        private val localDateClock: LocalDateClock,
        private val instantClock: InstantClock,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private var lastFetchedWeek: Int? = null
        private val _uiState = MutableStateFlow(PregnancyHubUiState())
        val uiState: StateFlow<PregnancyHubUiState> = _uiState.asStateFlow()

        private var tickerJob: Job? = null
        private var tickerKickSnapshot: KickTimerSnapshot = KickTimerSnapshot(null, null)
        private var tickerContractionSnapshot: ContractionTimerSnapshot =
            ContractionTimerSnapshot(null, null, null)
        private val hubTickerEnabled: Boolean = savedStateHandle.get<Boolean>(HUB_TICKER_ENABLED_KEY) ?: true

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
                }.collect { inputs ->
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
                                asOfDate = localDateClock.today(),
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
                    tickerKickSnapshot = kickTimer
                    tickerContractionSnapshot = contractionTimer
                    viewModelScope.launch { refreshKickCount(kickSessionId) }
                    restartTicker()
                }
            }
        }

        fun startKickSession() {
            viewModelScope.launch {
                val pregnancy = _uiState.value.activePregnancy ?: return@launch
                val session = repository.startKickSession(pregnancy.id)
                timerPreferences.setActiveKickSession(session.id, session.startedAtEpochMs)
                tickerKickSnapshot = KickTimerSnapshot(session.id, session.startedAtEpochMs)
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
                    tickerContractionSnapshot =
                        ContractionTimerSnapshot(sessionId, event.id, event.startedAtEpochMs)
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

        private suspend fun refreshKickCount(sessionId: String?) {
            if (sessionId == null) {
                _uiState.update { it.copy(kickCount = 0) }
                return
            }
            val count = repository.kickCount(sessionId)
            _uiState.update { it.copy(kickCount = count) }
        }

        internal suspend fun refreshTimerDisplayForTests() {
            val kickTimer = timerPreferences.observeKickTimer().first()
            val contractionTimer = timerPreferences.observeContractionTimer().first()
            applyTimerElapsed(kickTimer, contractionTimer)
        }

        private fun restartTicker() {
            if (!hubTickerEnabled) return
            tickerJob?.cancel()
            tickerJob =
                viewModelScope.launch {
                    while (isActive) {
                        applyTimerElapsed(tickerKickSnapshot, tickerContractionSnapshot)
                        delay(1_000L)
                    }
                }
        }

        private fun applyTimerElapsed(
            kickTimer: KickTimerSnapshot,
            contractionTimer: ContractionTimerSnapshot,
        ) {
            val now = instantClock.now().toEpochMilli()
            val kickElapsed =
                PregnancyHubTimerMath.elapsedSeconds(now, kickTimer.startedAtEpochMs)
            val contractionElapsed =
                PregnancyHubTimerMath.elapsedSeconds(
                    now,
                    contractionTimer.openEventStartedEpochMs,
                )
            _uiState.update {
                it.copy(
                    kickElapsedSeconds = kickElapsed,
                    contractionElapsedSeconds = contractionElapsed,
                )
            }
        }

        companion object {
            const val HUB_TICKER_ENABLED_KEY: String = "hub_ticker_enabled"
        }

        private data class HubInputs(
            val mode: ReproductiveMode?,
            val pregnancy: PregnancyRecordEntity?,
            val kickTimer: KickTimerSnapshot,
            val contractionTimer: ContractionTimerSnapshot,
        )
    }
