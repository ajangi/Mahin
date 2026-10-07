package dev.mahin.android.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.config.FeatureFlagGateway
import dev.mahin.core.config.FeatureFlagRepository
import dev.mahin.core.config.MahinFeatureFlags
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val reproductiveMode: ReproductiveMode = ReproductiveMode.CYCLE_TRACKING,
    val hasActivePregnancy: Boolean = false,
    val showPregnancyStartSheet: Boolean = false,
    val modeChangeBlockedMessage: Boolean = false,
    val postPregnancyTransition: Boolean = false,
    val healthConnectEntryVisible: Boolean = false,
    val healthAssistantEntryVisible: Boolean = false,
)

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val pregnancyRepository: PregnancyTrackingRepository,
        private val featureFlagGateway: FeatureFlagGateway,
        private val featureFlagRepository: FeatureFlagRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                featureFlagRepository.refreshFromRemote()
                _uiState.update {
                    it.copy(
                        healthConnectEntryVisible =
                            featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_CONNECT),
                        healthAssistantEntryVisible =
                            featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_ASSISTANT),
                    )
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
                    _uiState.update {
                        it.copy(
                            reproductiveMode = mode,
                            hasActivePregnancy = pregnancy != null && mode == ReproductiveMode.PREGNANT,
                            postPregnancyTransition = mode == ReproductiveMode.POST_PREGNANCY_TRANSITION,
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

        fun resumeCycleTracking() {
            viewModelScope.launch {
                pregnancyRepository.resumeTracking(ReproductiveMode.CYCLE_TRACKING)
            }
        }

        fun resumeTtc() {
            viewModelScope.launch {
                pregnancyRepository.resumeTracking(ReproductiveMode.TRYING_TO_CONCEIVE)
            }
        }
    }
