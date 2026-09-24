package dev.mahin.android.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.datastore.GuestIdentityRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RootUiState(
    val isLoading: Boolean = true,
    val onboardingComplete: Boolean = false,
)

@HiltViewModel
class RootViewModel
    @Inject
    constructor(
        private val cycleRepository: CycleTrackingRepository,
        private val guestIdentityRepository: GuestIdentityRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(RootUiState())
        val uiState: StateFlow<RootUiState> = _uiState.asStateFlow()

        init {
            refreshOnboardingState()
        }

        fun ensureGuestIdentity() {
            viewModelScope.launch {
                guestIdentityRepository.ensureGuestIdentity()
            }
        }

        fun refreshOnboardingState() {
            viewModelScope.launch {
                val profile = cycleRepository.getProfile()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        onboardingComplete = profile?.onboardingCompleted == true,
                    )
                }
            }
        }
    }
