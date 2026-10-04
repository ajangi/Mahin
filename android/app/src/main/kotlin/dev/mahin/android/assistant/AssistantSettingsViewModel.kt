package dev.mahin.android.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.config.FeatureFlagGateway
import dev.mahin.core.config.FeatureFlagRepository
import dev.mahin.core.config.MahinFeatureFlags
import dev.mahin.domain.assistant.AssistantConsentScopes
import dev.mahin.domain.assistant.HealthAssistantGateway
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AssistantConsentStatus {
    SAVED,
    ERROR,
}

data class AssistantSettingsUiState(
    val launchFlagLoading: Boolean = true,
    val launchFlagEnabled: Boolean = false,
    val shareCycleSummary: Boolean = false,
    val shareSymptomTags: Boolean = false,
    val statusMessage: AssistantConsentStatus? = null,
    val saving: Boolean = false,
)

@HiltViewModel
class AssistantSettingsViewModel
    @Inject
    constructor(
        private val featureFlagGateway: FeatureFlagGateway,
        private val featureFlagRepository: FeatureFlagRepository,
        private val healthAssistantGateway: HealthAssistantGateway,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AssistantSettingsUiState())
        val uiState: StateFlow<AssistantSettingsUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                featureFlagRepository.refreshFromRemote()
                val enabled = featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_ASSISTANT)
                _uiState.update {
                    it.copy(
                        launchFlagLoading = false,
                        launchFlagEnabled = enabled,
                    )
                }
                if (enabled) {
                    healthAssistantGateway.refreshConsent().onSuccess { consent ->
                        _uiState.update {
                            it.copy(
                                shareCycleSummary = consent.scopes.shareCycleSummary,
                                shareSymptomTags = consent.scopes.shareSymptomTags,
                            )
                        }
                    }
                }
            }
        }

        fun setShareCycleSummary(enabled: Boolean) {
            _uiState.update { it.copy(shareCycleSummary = enabled) }
        }

        fun setShareSymptomTags(enabled: Boolean) {
            _uiState.update { it.copy(shareSymptomTags = enabled) }
        }

        fun saveConsent() {
            viewModelScope.launch {
                _uiState.update { it.copy(saving = true, statusMessage = null) }
                val scopes =
                    AssistantConsentScopes(
                        shareCycleSummary = _uiState.value.shareCycleSummary,
                        shareSymptomTags = _uiState.value.shareSymptomTags,
                    )
                healthAssistantGateway
                    .updateConsent(scopes)
                    .onSuccess {
                        _uiState.update { state ->
                            state.copy(
                                saving = false,
                                statusMessage = AssistantConsentStatus.SAVED,
                            )
                        }
                    }.onFailure {
                        _uiState.update { state ->
                            state.copy(
                                saving = false,
                                statusMessage = AssistantConsentStatus.ERROR,
                            )
                        }
                    }
            }
        }
    }
