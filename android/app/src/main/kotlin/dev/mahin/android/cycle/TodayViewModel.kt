package dev.mahin.android.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.config.FeatureFlagGateway
import dev.mahin.core.config.FeatureFlagRepository
import dev.mahin.core.config.MahinFeatureFlags
import dev.mahin.core.database.cycle.CycleDashboard
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.pregnancy.PregnancyDatingEngineV1
import dev.mahin.domain.pregnancy.PregnancyStatusSnapshot
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TodayUiState(
    val dashboard: CycleDashboard? = null,
    val reproductiveMode: ReproductiveMode = ReproductiveMode.CYCLE_TRACKING,
    val pregnancyStatus: PregnancyStatusSnapshot? = null,
    val hasActivePregnancy: Boolean = false,
    val showPregnancyStartSheet: Boolean = false,
    val modeChangeBlockedMessage: Boolean = false,
    val healthConnectEntryVisible: Boolean = false,
)

@HiltViewModel
class TodayViewModel
    @Inject
    constructor(
        repository: CycleTrackingRepository,
        private val pregnancyRepository: PregnancyTrackingRepository,
        private val featureFlagGateway: FeatureFlagGateway,
        private val featureFlagRepository: FeatureFlagRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(TodayUiState())
        val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                featureFlagRepository.refreshFromRemote()
                _uiState.update {
                    it.copy(
                        healthConnectEntryVisible =
                            featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_CONNECT),
                    )
                }
            }
            viewModelScope.launch {
                repository.observeDashboard().collect { dashboard ->
                    _uiState.update { it.copy(dashboard = dashboard) }
                }
            }
            viewModelScope.launch {
                combine(
                    pregnancyRepository.observeProfile(),
                    pregnancyRepository.observeActivePregnancy(),
                ) { profile, pregnancy ->
                    profile to pregnancy
                }.collect { (profile, pregnancy) ->
                    val mode = profile?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING
                    val status =
                        pregnancy?.let {
                            PregnancyDatingEngineV1.status(
                                lmpDate = it.lmpDate,
                                clinicalEddDate = it.clinicalEddDate,
                                asOfDate = LocalDate.now(),
                            )
                        }
                    _uiState.update {
                        it.copy(
                            reproductiveMode = mode,
                            pregnancyStatus = status,
                            hasActivePregnancy = pregnancy != null && mode == ReproductiveMode.PREGNANT,
                        )
                    }
                }
            }
        }

        fun onReproductiveModeSelected(mode: ReproductiveMode) {
            viewModelScope.launch {
                val state = _uiState.value
                if (state.hasActivePregnancy && mode != ReproductiveMode.PREGNANT) {
                    _uiState.update { it.copy(modeChangeBlockedMessage = true) }
                    return@launch
                }
                _uiState.update { it.copy(modeChangeBlockedMessage = false) }
                when (mode) {
                    ReproductiveMode.PREGNANT -> {
                        if (state.hasActivePregnancy) {
                            pregnancyRepository.updateReproductiveMode(ReproductiveMode.PREGNANT)
                        } else {
                            _uiState.update { it.copy(showPregnancyStartSheet = true) }
                        }
                    }
                    ReproductiveMode.CYCLE_TRACKING,
                    ReproductiveMode.TRYING_TO_CONCEIVE,
                    -> pregnancyRepository.updateReproductiveMode(mode)
                    else -> Unit
                }
            }
        }

        fun dismissPregnancyStartSheet() {
            _uiState.update { it.copy(showPregnancyStartSheet = false) }
        }

        fun confirmPregnancyStart(
            lmpDate: LocalDate,
            clinicalEdd: LocalDate?,
        ) {
            viewModelScope.launch {
                pregnancyRepository.startPregnancy(
                    lmpDate = lmpDate,
                    clinicalEddDate = clinicalEdd,
                    datingReason = null,
                )
                _uiState.update { it.copy(showPregnancyStartSheet = false) }
            }
        }

        fun dismissModeBlockedMessage() {
            _uiState.update { it.copy(modeChangeBlockedMessage = false) }
        }
    }
