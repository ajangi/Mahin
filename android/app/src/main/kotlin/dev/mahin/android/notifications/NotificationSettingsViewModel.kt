package dev.mahin.android.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.datastore.NotificationPreferencesRepository
import dev.mahin.core.datastore.NotificationPrivacyMode
import dev.mahin.core.notifications.ReminderCoordinator
import dev.mahin.domain.reminders.ReminderCategory
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationSettingsUiState(
    val privacyMode: NotificationPrivacyMode = NotificationPrivacyMode.DISCREET,
    val categoryEnabled: Map<ReminderCategory, Boolean> = ReminderCategory.entries.associateWith { false },
    val pendingPermissionCategory: ReminderCategory? = null,
)

@HiltViewModel
class NotificationSettingsViewModel
    @Inject
    constructor(
        private val preferencesRepository: NotificationPreferencesRepository,
        private val reminderCoordinator: ReminderCoordinator,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(NotificationSettingsUiState())
        val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                preferencesRepository.observeSnapshot().collect { snapshot ->
                    _uiState.update {
                        it.copy(
                            privacyMode = snapshot.privacyMode,
                            categoryEnabled = snapshot.categoryEnabled,
                        )
                    }
                }
            }
        }

        fun onPrivacyModeSelected(mode: NotificationPrivacyMode) {
            viewModelScope.launch {
                preferencesRepository.setPrivacyMode(mode)
                reminderCoordinator.requestRefresh()
            }
        }

        fun onCategoryToggle(
            category: ReminderCategory,
            enabled: Boolean,
        ) {
            if (enabled) {
                _uiState.update { it.copy(pendingPermissionCategory = category) }
            } else {
                viewModelScope.launch {
                    preferencesRepository.setCategoryEnabled(category, false)
                    reminderCoordinator.requestRefresh()
                }
            }
        }

        fun onNotificationPermissionResult(granted: Boolean) {
            val category = _uiState.value.pendingPermissionCategory ?: return
            _uiState.update { it.copy(pendingPermissionCategory = null) }
            viewModelScope.launch {
                preferencesRepository.setCategoryEnabled(category, granted)
                reminderCoordinator.requestRefresh()
            }
        }

        fun dismissPermissionPrompt() {
            _uiState.update { it.copy(pendingPermissionCategory = null) }
        }
    }
