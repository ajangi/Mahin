package dev.mahin.android.learn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.analytics.AnalyticsEvent
import dev.mahin.core.analytics.AnalyticsGateway
import dev.mahin.core.common.InstantClock
import dev.mahin.core.common.RandomUuidFactory
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.content.ProgramSessionDto
import dev.mahin.core.content.WellnessContentLocalStore
import dev.mahin.domain.content.GuidedPlayerSnapshot
import dev.mahin.domain.content.GuidedPlayerStep
import dev.mahin.domain.content.GuidedSessionPlayer
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
@Suppress("LongParameterList")
class GuidedPlayerViewModel internal constructor(
    savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val localStore: WellnessContentLocalStore,
    private val playerStatePersistence: GuidedPlayerStatePersistence,
    private val clock: InstantClock,
    private val analytics: AnalyticsGateway,
    private val ticker: PlayerSessionTicker,
) : ViewModel() {
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        contentRepository: ContentRepository,
        localStore: WellnessContentLocalStore,
        clock: InstantClock,
        analytics: AnalyticsGateway,
        ticker: PlayerSessionTicker,
    ) : this(
        savedStateHandle = savedStateHandle,
        contentRepository = contentRepository,
        localStore = localStore,
        playerStatePersistence = localStore.asGuidedPlayerStatePersistence(),
        clock = clock,
        analytics = analytics,
        ticker = ticker,
    )

    private val programId: String = savedStateHandle.get<String>("programId") ?: ""
    private val sessionId: String = savedStateHandle.get<String>("sessionId") ?: ""
    private var session: ProgramSessionDto? = null
    private var player: GuidedSessionPlayer? = null
    private var tickJob: Job? = null
    private var lastPersistFingerprint: GuidedPlayerPersistPolicy.Fingerprint? = null
    private var persistEnabled: Boolean = true

    private val _uiState =
        MutableStateFlow(
            GuidedPlayerUiState(
                sessionTitle = "",
                stepInstruction = "",
                stepIndex = 0,
                stepCount = 1,
                progress = 0f,
                paused = false,
                completed = false,
                snapshot = GuidedPlayerSnapshot(0, 0, false, false),
            ),
        )
    val uiState: StateFlow<GuidedPlayerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadSession()
        }
    }

    private suspend fun loadSession() {
        val program = contentRepository.program(programId)
        if (program == null) {
            _uiState.update {
                it.copy(offline = true, loadError = true, stepCount = 0, stepInstruction = "")
            }
            return
        }
        session = program.sessions.firstOrNull { it.id == sessionId }
        val steps =
            session?.steps?.map {
                GuidedPlayerStep(
                    instruction = it.instruction,
                    durationSeconds = it.durationSeconds,
                    reps = it.reps,
                    restSeconds = it.restSeconds,
                )
            } ?: emptyList()
        if (steps.isEmpty()) {
            _uiState.update {
                it.copy(
                    offline = true,
                    loadError = true,
                    sessionTitle = session?.title ?: "",
                    stepCount = 0,
                )
            }
            return
        }
        val engine = GuidedSessionPlayer(steps, clock)
        playerStatePersistence.loadPlayerState()?.let { saved ->
            if (saved.programId == programId && saved.sessionId == sessionId) {
                engine.restore(
                    GuidedPlayerSnapshot(
                        stepIndex = saved.stepIndex,
                        stepElapsedMs = saved.stepElapsedMs,
                        paused = saved.paused,
                        completed = false,
                    ),
                )
            }
        }
        player = engine
        lastPersistFingerprint = GuidedPlayerPersistPolicy.fingerprint(engine.snapshot())
        analytics.track(AnalyticsEvent("program_session_started", mapOf("surface" to "learn")))
        publish()
        startTicks()
    }

    fun pause() {
        player?.pause()
        publish()
        persistSnapshot(force = true)
    }

    fun resume() {
        player?.resume()
        publish()
        persistSnapshot(force = true)
    }

    fun completeSession() {
        viewModelScope.launch {
            val currentSession = session ?: return@launch
            persistEnabled = false
            tickJob?.cancel()
            tickJob = null
            localStore.logSessionCompletion(
                id = RandomUuidFactory.random().toString(),
                programId = programId,
                sessionId = currentSession.id,
            )
            playerStatePersistence.clearPlayerState()
            lastPersistFingerprint = null
            analytics.track(AnalyticsEvent("program_session_completed", mapOf("surface" to "learn")))
        }
    }

    fun stopSession() {
        persistEnabled = false
        tickJob?.cancel()
        tickJob = null
        viewModelScope.launch {
            playerStatePersistence.clearPlayerState()
            lastPersistFingerprint = null
        }
    }

    private fun startTicks() {
        tickJob?.cancel()
        tickJob =
            viewModelScope.launch {
                while (isActive) {
                    player?.tick()
                    val snap = player?.snapshot() ?: break
                    publish()
                    if (GuidedPlayerPersistPolicy.shouldPersistAfterTick(snap, lastPersistFingerprint)) {
                        persistSnapshot(force = false)
                    }
                    ticker.waitNextTick()
                }
            }
    }

    private fun persistSnapshot(force: Boolean) {
        if (!persistEnabled) return
        val engine = player ?: return
        val snap = engine.snapshot()
        val fingerprint = GuidedPlayerPersistPolicy.fingerprint(snap)
        if (!force && fingerprint == lastPersistFingerprint) return
        lastPersistFingerprint = fingerprint
        viewModelScope.launch {
            playerStatePersistence.savePlayerState(
                programId = programId,
                sessionId = sessionId,
                stepIndex = snap.stepIndex,
                stepElapsedMs = snap.stepElapsedMs,
                paused = snap.paused,
            )
        }
    }

    private fun publish() {
        val engine = player ?: return
        val snap = engine.snapshot()
        val steps = session?.steps ?: emptyList()
        val current = steps.getOrNull(snap.stepIndex)
        val durationMs = (current?.durationSeconds ?: 0).coerceAtLeast(1) * 1000f
        val progress =
            if (durationMs > 0) {
                (snap.stepElapsedMs / durationMs).coerceIn(0f, 1f)
            } else {
                0f
            }
        _uiState.update {
            it.copy(
                sessionTitle = session?.title ?: "",
                stepInstruction = current?.instruction ?: "",
                stepIndex = snap.stepIndex,
                stepCount = steps.size.coerceAtLeast(1),
                progress = progress,
                paused = snap.paused,
                completed = snap.completed,
                snapshot = snap,
                loadError = false,
                offline = false,
            )
        }
    }

    override fun onCleared() {
        if (persistEnabled) {
            persistSnapshot(force = true)
        }
        tickJob?.cancel()
        super.onCleared()
    }
}
