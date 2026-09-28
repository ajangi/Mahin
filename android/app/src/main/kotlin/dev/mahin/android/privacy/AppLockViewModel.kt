package dev.mahin.android.privacy

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
import androidx.lifecycle.viewModelScope

data class AppLockUiState(
    val visible: Boolean = false,
    val mode: AppLockMode = AppLockMode.DISABLED,
    val pinEntry: String = "",
    val error: Boolean = false,
)

@HiltViewModel
class AppLockViewModel
    @Inject
    constructor(
        private val appLockGateway: AppLockGateway,
        private val pinCredentialStore: PinCredentialStore,
        private val appLockPreferencesRepository: AppLockPreferencesRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(AppLockUiState())
        val uiState: StateFlow<AppLockUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                appLockPreferencesRepository.snapshot.collect { snapshot ->
                    val needsLock =
                        snapshot.mode != AppLockMode.DISABLED &&
                            !appLockGateway.isSessionUnlocked()
                    _uiState.update {
                        it.copy(
                            visible = needsLock,
                            mode = snapshot.mode,
                        )
                    }
                }
            }
        }

        fun onPinChange(value: String) {
            _uiState.update { it.copy(pinEntry = value.filter { it.isDigit() }.take(8), error = false) }
        }

        fun submitPin() {
            val pin = _uiState.value.pinEntry
            if (pinCredentialStore.verifyPin(pin)) {
                appLockGateway.markSessionUnlocked()
                _uiState.update { it.copy(visible = false, pinEntry = "", error = false) }
            } else {
                _uiState.update { it.copy(error = true) }
            }
        }

        fun launchBiometric(
            activity: FragmentActivity,
            onSuccess: () -> Unit,
        ) {
            val prompt =
                androidx.biometric.BiometricPrompt(
                    activity,
                    androidx.core.content.ContextCompat.getMainExecutor(activity),
                    object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                            appLockGateway.markSessionUnlocked()
                            _uiState.update { it.copy(visible = false, pinEntry = "", error = false) }
                            onSuccess()
                        }
                    },
                )
            val info =
                androidx.biometric.BiometricPrompt.PromptInfo
                    .Builder()
                    .setTitle(activity.getString(dev.mahin.android.R.string.app_lock_title))
                    .setNegativeButtonText(activity.getString(dev.mahin.android.R.string.privacy_back))
                    .build()
            prompt.authenticate(info)
        }
    }
