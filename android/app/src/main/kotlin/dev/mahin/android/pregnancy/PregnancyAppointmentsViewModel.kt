package dev.mahin.android.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.database.entity.PregnancyAppointmentEntity
import dev.mahin.core.database.entity.PregnancyRecordEntity
import dev.mahin.core.database.pregnancy.PregnancyAppointmentInput
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.datastore.NotificationPreferencesRepository
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.model.PregnancyAppointmentType
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.core.notifications.ReminderCoordinator
import dev.mahin.domain.reminders.ReminderCategory
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PregnancyAppointmentsUiState(
    val isLoading: Boolean = true,
    val hasActivePregnancy: Boolean = false,
    val activePregnancy: PregnancyRecordEntity? = null,
    val appointments: List<PregnancyAppointmentEntity> = emptyList(),
    val newAppointmentTitle: String = "",
    val newAppointmentType: PregnancyAppointmentType = PregnancyAppointmentType.CLINICIAN_VISIT,
    val newAppointmentJalali: JalaliDate = PersianCivilDateConverter.toJalali(LocalDate.now()),
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class PregnancyAppointmentsViewModel
    @Inject
    constructor(
        private val repository: PregnancyTrackingRepository,
        private val notificationPreferencesRepository: NotificationPreferencesRepository,
        private val reminderCoordinator: ReminderCoordinator,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PregnancyAppointmentsUiState())
        val uiState: StateFlow<PregnancyAppointmentsUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                repository
                    .observeProfile()
                    .flatMapLatest { profile ->
                        val mode = profile?.reproductiveMode
                        val pregnant = mode == ReproductiveMode.PREGNANT
                        repository.observeActivePregnancy().flatMapLatest { pregnancy ->
                            val appointmentFlow =
                                if (pregnant && pregnancy != null) {
                                    repository.observeAppointments(pregnancy.id)
                                } else {
                                    flowOf(emptyList())
                                }
                            appointmentFlow.map { appointments ->
                                Triple(pregnant, pregnancy, appointments)
                            }
                        }
                    }.collect { (pregnant, pregnancy, appointments) ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                hasActivePregnancy = pregnant && pregnancy != null,
                                activePregnancy = pregnancy,
                                appointments = appointments,
                            )
                        }
                    }
            }
        }

        fun onNewAppointmentTitleChange(value: String) {
            _uiState.update { it.copy(newAppointmentTitle = value) }
        }

        fun onNewAppointmentTypeSelected(type: PregnancyAppointmentType) {
            _uiState.update { it.copy(newAppointmentType = type) }
        }

        fun onNewAppointmentDateSelected(jalali: JalaliDate) {
            _uiState.update { it.copy(newAppointmentJalali = jalali) }
        }

        fun addAppointment() {
            viewModelScope.launch {
                val state = _uiState.value
                val pregnancy = state.activePregnancy ?: return@launch
                val title = state.newAppointmentTitle.trim()
                if (title.isEmpty()) return@launch
                val date = PersianCivilDateConverter.toGregorian(state.newAppointmentJalali)
                val epochMs = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val appointmentRemindersEnabled =
                    notificationPreferencesRepository
                        .observeCategoryEnabled(ReminderCategory.APPOINTMENT)
                        .first()
                repository.upsertAppointment(
                    PregnancyAppointmentInput(
                        id = null,
                        pregnancyId = pregnancy.id,
                        appointmentType = state.newAppointmentType,
                        title = title,
                        scheduledAtEpochMs = epochMs,
                        location = null,
                        clinicianName = null,
                        note = null,
                        reminderEnabled = appointmentRemindersEnabled,
                    ),
                )
                reminderCoordinator.requestRefresh()
                _uiState.update { it.copy(newAppointmentTitle = "") }
            }
        }
    }
