package dev.mahin.android.healthconnect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.config.FeatureFlagGateway
import dev.mahin.core.config.FeatureFlagRepository
import dev.mahin.core.config.MahinFeatureFlags
import dev.mahin.core.datastore.HealthConnectPreferencesRepository
import dev.mahin.core.healthconnect.HealthConnectAvailability
import dev.mahin.core.healthconnect.HealthConnectClientGateway
import dev.mahin.core.healthconnect.HealthConnectCoordinator
import dev.mahin.core.healthconnect.HealthConnectPermissionEducation
import dev.mahin.core.healthconnect.HealthConnectPermissionPolicy
import dev.mahin.core.healthconnect.HealthConnectSyncResult
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HealthConnectUiState(
    val launchFlagEnabled: Boolean = false,
    val userOptIn: Boolean = false,
    val availability: HealthConnectAvailability = HealthConnectAvailability.FEATURE_DISABLED,
    val permissionsGranted: Boolean = false,
    val educationAcknowledged: Boolean = false,
    val statusMessage: String? = null,
    val busy: Boolean = false,
    val locked: Boolean = false,
)

@HiltViewModel
class HealthConnectSettingsViewModel
    @Inject
    constructor(
        private val featureFlagGateway: FeatureFlagGateway,
        private val featureFlagRepository: FeatureFlagRepository,
        private val preferencesRepository: HealthConnectPreferencesRepository,
        private val coordinator: HealthConnectCoordinator,
        private val clientGateway: HealthConnectClientGateway,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HealthConnectUiState())
        val uiState: StateFlow<HealthConnectUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                featureFlagRepository.refreshFromRemote()
                preferencesRepository.snapshot.collect { prefs ->
                    refreshInternal(prefs.userOptIn)
                }
            }
        }

        fun refresh() {
            viewModelScope.launch {
                featureFlagRepository.refreshFromRemote()
                when (coordinator.refreshRevocationState()) {
                    HealthConnectSyncResult.PermissionsRevoked ->
                        _uiState.update { it.copy(statusMessage = STATUS_REVOKED) }
                    else -> Unit
                }
            }
        }

        private suspend fun refreshInternal(userOptIn: Boolean) {
            val launchEnabled = featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_CONNECT)
            val availability =
                if (launchEnabled) {
                    clientGateway.availability()
                } else {
                    HealthConnectAvailability.FEATURE_DISABLED
                }
            val granted =
                if (launchEnabled) {
                    HealthConnectPermissionPolicy.hasAllGranted(clientGateway.grantedPermissionStrings())
                } else {
                    false
                }
            _uiState.update {
                it.copy(
                    launchFlagEnabled = launchEnabled,
                    userOptIn = userOptIn,
                    availability = availability,
                    permissionsGranted = granted,
                )
            }
        }

        fun setUserOptIn(enabled: Boolean) {
            viewModelScope.launch {
                coordinator.setUserOptIn(enabled)
                if (!enabled) {
                    _uiState.update { it.copy(educationAcknowledged = false, statusMessage = null) }
                }
            }
        }

        fun acknowledgeEducation() {
            _uiState.update { it.copy(educationAcknowledged = true) }
        }

        fun onPermissionsResult(granted: Set<String>) {
            viewModelScope.launch {
                val allGranted = HealthConnectPermissionPolicy.hasAllGranted(granted)
                _uiState.update { it.copy(permissionsGranted = allGranted) }
                if (allGranted) {
                    preferencesRepository.setPermissionsPreviouslyGranted(true)
                }
            }
        }

        fun importNow() {
            runSync { coordinator.importFromHealthConnect() }
        }

        fun exportNow() {
            runSync { coordinator.exportToHealthConnect() }
        }

        private fun runSync(block: suspend () -> HealthConnectSyncResult) {
            viewModelScope.launch {
                _uiState.update { it.copy(busy = true, locked = false, statusMessage = null) }
                when (val result = block()) {
                    HealthConnectSyncResult.Locked ->
                        _uiState.update { it.copy(busy = false, locked = true) }
                    is HealthConnectSyncResult.Success ->
                        _uiState.update {
                            it.copy(
                                busy = false,
                                statusMessage =
                                    "همگام‌سازی انجام شد (وارد: ${result.importedDays}، صادر: ${result.exportedDays}).",
                            )
                        }
                    HealthConnectSyncResult.PermissionsMissing ->
                        _uiState.update {
                            it.copy(busy = false, statusMessage = STATUS_PERMISSIONS)
                        }
                    HealthConnectSyncResult.PermissionsRevoked ->
                        _uiState.update {
                            it.copy(busy = false, statusMessage = STATUS_REVOKED)
                        }
                    HealthConnectSyncResult.NotOptedIn ->
                        _uiState.update { it.copy(busy = false) }
                    HealthConnectSyncResult.FeatureDisabled ->
                        _uiState.update { it.copy(busy = false, launchFlagEnabled = false) }
                    is HealthConnectSyncResult.Failure ->
                        _uiState.update {
                            it.copy(busy = false, statusMessage = STATUS_GENERIC_ERROR)
                        }
                }
            }
        }

        fun permissionRationale(): List<dev.mahin.core.healthconnect.HealthConnectPermissionRationale> =
            HealthConnectPermissionEducation.permissionRationaleFa

        companion object {
            const val STATUS_REVOKED =
                "دسترسی Health Connect لغو شد. همگام‌سازی متوقف شد؛ می‌توانید دوباره فعال کنید."
            const val STATUS_PERMISSIONS = "ابتدا دسترسی‌های خواسته‌شده را تأیید کنید."
            const val STATUS_GENERIC_ERROR = "همگام‌سازی انجام نشد. بعداً دوباره تلاش کنید."
        }
    }
