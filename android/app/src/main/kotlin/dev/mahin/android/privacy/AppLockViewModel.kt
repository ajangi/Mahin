package dev.mahin.android.privacy

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.datastore.AppLockMode
import dev.mahin.core.datastore.AppLockPreferencesRepository
import dev.mahin.core.security.AppLockGateway
import dev.mahin.core.security.PinCredentialStore
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppLockUiState(
    val showLoading: Boolean = true,
    val visible: Boolean = true,
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
                combine(
                    appLockPreferencesRepository.snapshot,
                    appLockGateway.sessionRevision,
                ) { snapshot, _ ->
                    applyGatewayState(snapshot.mode)
                }.collectLatest { state ->
                    _uiState.value = state
                }
            }
        }

        fun onAppForeground() {
            _uiState.update { current ->
                current.copy(
                    showLoading = !appLockGateway.arePreferencesLoaded(),
                    visible = appLockGateway.shouldShowLockGate(),
                )
            }
        }

        fun onPinChange(value: String) {
            _uiState.update { it.copy(pinEntry = value.filter { it.isDigit() }.take(8), error = false) }
        }

        fun submitPin() {
            val pin = _uiState.value.pinEntry
            if (pinCredentialStore.verifyPin(pin)) {
                appLockGateway.markSessionUnlocked()
                _uiState.update { applyGatewayState(_uiState.value.mode) }
            } else {
                _uiState.update { it.copy(error = true) }
            }
        }

        fun launchBiometric(
            activity: FragmentActivity,
            onSuccess: () -> Unit,
        ) {
            val prompt =
                BiometricPrompt(
                    activity,
                    ContextCompat.getMainExecutor(activity),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            appLockGateway.markSessionUnlocked()
                            _uiState.update { applyGatewayState(_uiState.value.mode) }
                            onSuccess()
                        }
                    },
                )
            val info =
                BiometricPrompt.PromptInfo
                    .Builder()
                    .setTitle(activity.getString(dev.mahin.android.R.string.app_lock_title))
                    .setNegativeButtonText(activity.getString(dev.mahin.android.R.string.privacy_back))
                    .build()
            prompt.authenticate(info)
        }

        private fun applyGatewayState(mode: AppLockMode): AppLockUiState {
            val showLoading = !appLockGateway.arePreferencesLoaded()
            val visible = appLockGateway.shouldShowLockGate()
            return AppLockUiState(
                showLoading = showLoading,
                visible = visible,
                mode = mode,
                pinEntry = "",
                error = false,
            )
        }
    }
