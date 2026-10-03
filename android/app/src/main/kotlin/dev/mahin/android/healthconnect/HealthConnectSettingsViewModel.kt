package dev.mahin.android.healthconnect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.android.R
import dev.mahin.core.config.FeatureFlagGateway
import dev.mahin.core.config.FeatureFlagRepository
import dev.mahin.core.config.MahinFeatureFlags
import dev.mahin.core.datastore.HealthConnectPreferencesRepository
import dev.mahin.core.healthconnect.HealthConnectCoordinatorFacade
import dev.mahin.core.healthconnect.HealthConnectPermissionEducation
import dev.mahin.core.healthconnect.HealthConnectPermissionPolicy
import dev.mahin.domain.healthconnect.HealthConnectAvailability
import dev.mahin.domain.healthconnect.HealthConnectClientResult
import dev.mahin.domain.healthconnect.HealthConnectRemoteClient
import dev.mahin.domain.healthconnect.HealthConnectSyncResult
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HealthConnectUiState(
    val launchFlagLoading: Boolean = true,
    val launchFlagEnabled: Boolean = false,
    val userOptIn: Boolean = false,
    val availability: HealthConnectAvailability = HealthConnectAvailability.FEATURE_DISABLED,
    val permissionsGranted: Boolean = false,
    val educationAcknowledged: Boolean = false,
    val statusMessageRes: Int? = null,
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
        private val coordinator: HealthConnectCoordinatorFacade,
        private val remoteClient: HealthConnectRemoteClient,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HealthConnectUiState())
        val uiState: StateFlow<HealthConnectUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                featureFlagRepository.refreshFromRemote()
                val enabled = featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_CONNECT)
                _uiState.update {
                    it.copy(
                        launchFlagLoading = false,
                        launchFlagEnabled = enabled,
                    )
                }
                if (enabled) {
                    applyRevocationRefresh()
                }
                preferencesRepository.snapshot.collect { prefs ->
                    refreshUi(prefs.userOptIn)
                }
            }
        }

        fun onScreenOpened() {
            viewModelScope.launch { loadLaunchFlag() }
        }

        fun onScreenResumed() {
            viewModelScope.launch { applyRevocationRefresh() }
        }

        private suspend fun loadLaunchFlag() {
            featureFlagRepository.refreshFromRemote()
            val enabled = featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_CONNECT)
            _uiState.update {
                it.copy(
                    launchFlagLoading = false,
                    launchFlagEnabled = enabled,
                )
            }
            if (enabled) {
                applyRevocationRefresh()
            }
            refreshUi(preferencesRepository.snapshot.first().userOptIn)
        }

        private suspend fun applyRevocationRefresh() {
            if (!featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_CONNECT)) return
            when (coordinator.refreshRevocationState()) {
                HealthConnectSyncResult.PermissionsRevoked ->
                    _uiState.update {
                        it.copy(
                            statusMessageRes = R.string.health_connect_status_revoked,
                            userOptIn = false,
                            educationAcknowledged = false,
                        )
                    }
                else -> Unit
            }
        }

        private suspend fun refreshUi(userOptIn: Boolean) {
            if (_uiState.value.launchFlagLoading) return
            val launchEnabled = featureFlagGateway.isEnabled(MahinFeatureFlags.HEALTH_CONNECT)
            val availability =
                if (launchEnabled) {
                    remoteClient.availability()
                } else {
                    HealthConnectAvailability.FEATURE_DISABLED
                }
            val granted =
                if (launchEnabled && availability == HealthConnectAvailability.READY) {
                    when (val result = remoteClient.grantedPermissionStrings()) {
                        is HealthConnectClientResult.Ok ->
                            HealthConnectPermissionPolicy.hasAllGranted(result.value)
                        else -> false
                    }
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
                    _uiState.update {
                        it.copy(educationAcknowledged = false, statusMessageRes = null)
                    }
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
                _uiState.update { it.copy(busy = true, locked = false, statusMessageRes = null) }
                when (val result = block()) {
                    HealthConnectSyncResult.Locked ->
                        _uiState.update { it.copy(busy = false, locked = true) }
                    is HealthConnectSyncResult.Success ->
                        _uiState.update {
                            it.copy(
                                busy = false,
                                statusMessageRes = R.string.health_connect_status_sync_success,
                            )
                        }
                    HealthConnectSyncResult.PermissionsMissing ->
                        _uiState.update {
                            it.copy(busy = false, statusMessageRes = R.string.health_connect_status_permissions)
                        }
                    HealthConnectSyncResult.PermissionsRevoked ->
                        _uiState.update {
                            it.copy(
                                busy = false,
                                statusMessageRes = R.string.health_connect_status_revoked,
                                userOptIn = false,
                                educationAcknowledged = false,
                            )
                        }
                    HealthConnectSyncResult.NotOptedIn ->
                        _uiState.update { it.copy(busy = false) }
                    HealthConnectSyncResult.FeatureDisabled ->
                        _uiState.update { it.copy(busy = false, launchFlagEnabled = false) }
                    is HealthConnectSyncResult.Failure ->
                        _uiState.update {
                            it.copy(busy = false, statusMessageRes = R.string.health_connect_status_error)
                        }
                }
            }
        }

        fun permissionRationale(): List<dev.mahin.core.healthconnect.HealthConnectPermissionRationale> =
            HealthConnectPermissionEducation.permissionRationaleFa
    }
