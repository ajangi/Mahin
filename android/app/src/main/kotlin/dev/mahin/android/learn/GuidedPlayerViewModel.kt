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
import dev.mahin.domain.content.GuidedPlayerStep
import dev.mahin.domain.content.GuidedSessionPlayer
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class GuidedPlayerViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val contentRepository: ContentRepository,
        private val localStore: WellnessContentLocalStore,
        private val clock: InstantClock,
        private val analytics: AnalyticsGateway,
    ) : ViewModel() {
        private val programId: String = savedStateHandle.get<String>("programId") ?: ""
        private val sessionId: String = savedStateHandle.get<String>("sessionId") ?: ""
        private var session: ProgramSessionDto? = null
        private var player: GuidedSessionPlayer? = null
        private var tickJob: Job? = null

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
                    snapshot =
                        dev.mahin.domain.content
                            .GuidedPlayerSnapshot(0, 0, false, false),
                ),
            )
        val uiState: StateFlow<GuidedPlayerUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                val program = contentRepository.program(programId)
                session = program?.sessions?.firstOrNull { it.id == sessionId }
                val steps =
                    session?.steps?.map {
                        GuidedPlayerStep(
                            instruction = it.instruction,
                            durationSeconds = it.durationSeconds,
                            reps = it.reps,
                            restSeconds = it.restSeconds,
                        )
                    } ?: emptyList()
                val engine = GuidedSessionPlayer(steps, clock)
                localStore.loadPlayerState()?.let { saved ->
                    if (saved.programId == programId && saved.sessionId == sessionId) {
                        engine.restore(
                            dev.mahin.domain.content.GuidedPlayerSnapshot(
                                stepIndex = saved.stepIndex,
                                stepElapsedMs = saved.stepElapsedMs,
                                paused = saved.paused,
                                completed = false,
                            ),
                        )
                    }
                }
                player = engine
                analytics.track(AnalyticsEvent("program_session_started", mapOf("surface" to "learn")))
                publish()
                startTicks()
            }
        }

        fun pause() {
            player?.pause()
            persist()
            publish()
        }

        fun resume() {
            player?.resume()
            persist()
            publish()
        }

        fun completeSession() {
            viewModelScope.launch {
                val currentSession = session ?: return@launch
                localStore.logSessionCompletion(
                    id = RandomUuidFactory.random().toString(),
                    programId = programId,
                    sessionId = currentSession.id,
                )
                localStore.clearPlayerState()
                analytics.track(AnalyticsEvent("program_session_completed", mapOf("surface" to "learn")))
            }
        }

        private fun startTicks() {
            tickJob?.cancel()
            tickJob =
                viewModelScope.launch {
                    while (isActive) {
                        player?.tick()
                        publish()
                        persist()
                        delay(250)
                    }
                }
        }

        private fun persist() {
            val engine = player ?: return
            val snap = engine.snapshot()
            viewModelScope.launch {
                localStore.savePlayerState(
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
                )
            }
        }

        override fun onCleared() {
            tickJob?.cancel()
            super.onCleared()
        }
    }
