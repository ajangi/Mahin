package dev.mahin.android.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.database.cycle.CycleDashboard
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.ttc.TtcTrackingRepository
import dev.mahin.core.model.ReproductiveMode
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TodayUiState(
    val dashboard: CycleDashboard? = null,
    val reproductiveMode: ReproductiveMode = ReproductiveMode.CYCLE_TRACKING,
)

@HiltViewModel
class TodayViewModel
    @Inject
    constructor(
        repository: CycleTrackingRepository,
        private val ttcRepository: TtcTrackingRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(TodayUiState())
        val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                repository.observeDashboard().collect { dashboard ->
                    _uiState.update { it.copy(dashboard = dashboard) }
                }
            }
            viewModelScope.launch {
                ttcRepository.observeProfile().collect { profile ->
                    val mode = profile?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING
                    _uiState.update { it.copy(reproductiveMode = mode) }
                }
            }
        }

        fun onReproductiveModeSelected(mode: ReproductiveMode) {
            viewModelScope.launch {
                ttcRepository.updateReproductiveMode(mode)
            }
        }
    }
