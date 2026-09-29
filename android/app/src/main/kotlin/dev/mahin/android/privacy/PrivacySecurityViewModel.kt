package dev.mahin.android.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.database.LocalHealthDataErasureService
import dev.mahin.core.datastore.AppLockMode
import dev.mahin.core.datastore.AppLockPreferencesRepository
import dev.mahin.core.security.AppLockGateway
import dev.mahin.core.security.PinCredentialStore
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrivacySecurityUiState(
    val mode: AppLockMode = AppLockMode.DISABLED,
    val hideRecents: Boolean = true,
    val blockScreenshots: Boolean = true,
    val pinDraft: String = "",
    val message: String? = null,
    val eraseConfirmVisible: Boolean = false,
)

@HiltViewModel
class PrivacySecurityViewModel
    @Inject
    constructor(
        private val appLockPreferencesRepository: AppLockPreferencesRepository,
        private val pinCredentialStore: PinCredentialStore,
        private val appLockGateway: AppLockGateway,
        private val localHealthDataErasureService: LocalHealthDataErasureService,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PrivacySecurityUiState())
        val uiState: StateFlow<PrivacySecurityUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                appLockPreferencesRepository.snapshot.collect { snapshot ->
                    _uiState.update {
                        it.copy(
                            mode = snapshot.mode,
                            hideRecents = snapshot.hideRecentsWhenLocked,
                            blockScreenshots = snapshot.blockScreenshotsOnSensitiveScreens,
                        )
                    }
                }
            }
        }

        fun onPinDraftChange(value: String) {
            _uiState.update { it.copy(pinDraft = value.filter { char -> char.isDigit() }.take(8)) }
        }

        fun setMode(mode: AppLockMode) {
            viewModelScope.launch {
                if (mode != AppLockMode.DISABLED && !pinCredentialStore.hasPin()) {
                    _uiState.update { it.copy(message = "pin_required") }
                    return@launch
                }
                appLockPreferencesRepository.setMode(mode)
                if (mode == AppLockMode.DISABLED) {
                    pinCredentialStore.clearPin()
                    appLockGateway.markSessionUnlocked()
                } else {
                    appLockGateway.lockSession()
                }
            }
        }

        fun savePin() {
            val pin = _uiState.value.pinDraft
            if (pin.length < 4) {
                _uiState.update { it.copy(message = "pin_too_short") }
                return
            }
            pinCredentialStore.setPin(pin)
            viewModelScope.launch {
                appLockPreferencesRepository.setMode(AppLockMode.PIN)
                appLockGateway.lockSession()
                _uiState.update { it.copy(pinDraft = "", message = null) }
            }
        }

        fun setHideRecents(enabled: Boolean) {
            viewModelScope.launch {
                appLockPreferencesRepository.setHideRecentsWhenLocked(enabled)
            }
        }

        fun setBlockScreenshots(enabled: Boolean) {
            viewModelScope.launch {
                appLockPreferencesRepository.setBlockScreenshotsOnSensitiveScreens(enabled)
            }
        }

        fun showEraseConfirm() {
            _uiState.update { it.copy(eraseConfirmVisible = true) }
        }

        fun dismissEraseConfirm() {
            _uiState.update { it.copy(eraseConfirmVisible = false) }
        }

        fun eraseLocalData(onComplete: () -> Unit) {
            viewModelScope.launch {
                localHealthDataErasureService.eraseAllLocalHealthData()
                appLockPreferencesRepository.setMode(AppLockMode.DISABLED)
                pinCredentialStore.clearPin()
                appLockGateway.markSessionUnlocked()
                _uiState.update { it.copy(eraseConfirmVisible = false) }
                onComplete()
            }
        }
    }
