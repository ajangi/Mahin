package dev.mahin.android.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.android.cycle.CycleFormatters
import dev.mahin.core.common.LocalDateClock
import dev.mahin.core.content.ContentRepository
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
import dev.mahin.domain.pregnancy.PregnancyDatingEngineV1
import dev.mahin.domain.pregnancy.PregnancyPlanTimelineMerger
import dev.mahin.domain.reminders.ReminderCategory
import java.time.Instant
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

data class PregnancyPlanTimelineItemUi(
    val title: String,
    val subtitle: String,
    val isCarePlan: Boolean,
    val carePlanItemId: String? = null,
    val carePlanDocumentId: String? = null,
)

data class PregnancyAppointmentsUiState(
    val isLoading: Boolean = true,
    val hasActivePregnancy: Boolean = false,
    val activePregnancy: PregnancyRecordEntity? = null,
    val appointments: List<PregnancyAppointmentEntity> = emptyList(),
    val timeline: List<PregnancyPlanTimelineItemUi> = emptyList(),
    val newAppointmentTitle: String = "",
    val newAppointmentType: PregnancyAppointmentType = PregnancyAppointmentType.CLINICIAN_VISIT,
    val newAppointmentJalali: JalaliDate,
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class PregnancyAppointmentsViewModel
    @Inject
    constructor(
        private val repository: PregnancyTrackingRepository,
        private val contentRepository: ContentRepository,
        private val notificationPreferencesRepository: NotificationPreferencesRepository,
        private val reminderCoordinator: ReminderCoordinator,
        private val localDateClock: LocalDateClock,
    ) : ViewModel() {
        private var cachedCarePlan: List<PregnancyPlanTimelineMerger.CarePlanRow> = emptyList()
        private val _uiState =
            MutableStateFlow(
                PregnancyAppointmentsUiState(
                    newAppointmentJalali =
                        PersianCivilDateConverter.toJalali(localDateClock.today()),
                ),
            )
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
                        val timeline =
                            if (pregnant && pregnancy != null) {
                                buildTimeline(pregnancy, appointments)
                            } else {
                                emptyList()
                            }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                hasActivePregnancy = pregnant && pregnancy != null,
                                activePregnancy = pregnancy,
                                appointments = appointments,
                                timeline = timeline,
                            )
                        }
                        if (pregnant && pregnancy != null && cachedCarePlan.isEmpty()) {
                            viewModelScope.launch { refreshCarePlan(pregnancy) }
                        }
                    }
            }
        }

        private suspend fun refreshCarePlan(pregnancy: PregnancyRecordEntity) {
            val week =
                PregnancyDatingEngineV1
                    .status(
                        lmpDate = pregnancy.lmpDate,
                        clinicalEddDate = pregnancy.clinicalEddDate,
                        asOfDate = localDateClock.today(),
                    ).displayWeekNumber
            val schedules = contentRepository.careSchedules(gestationalWeek = week)
            cachedCarePlan =
                schedules.flatMap { schedule ->
                    schedule.items.map { item ->
                        PregnancyPlanTimelineMerger.CarePlanRow(
                            itemId = item.id,
                            documentId = schedule.id,
                            title = item.title,
                            weekStart = item.weekStart,
                            weekEnd = item.weekEnd,
                            category = item.category,
                        )
                    }
                }
            _uiState.update { state ->
                val pregnancyEntity = state.activePregnancy
                if (pregnancyEntity == null) {
                    state
                } else {
                    state.copy(timeline = buildTimeline(pregnancyEntity, state.appointments))
                }
            }
        }

        private fun buildTimeline(
            pregnancy: PregnancyRecordEntity,
            appointments: List<PregnancyAppointmentEntity>,
        ): List<PregnancyPlanTimelineItemUi> {
            val appointmentRows =
                appointments.map { entity ->
                    val date =
                        Instant
                            .ofEpochMilli(entity.scheduledAtEpochMs)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    val status =
                        PregnancyDatingEngineV1.status(
                            lmpDate = pregnancy.lmpDate,
                            clinicalEddDate = pregnancy.clinicalEddDate,
                            asOfDate = date,
                        )
                    PregnancyPlanTimelineMerger.AppointmentRow(
                        appointmentId = entity.id,
                        title = entity.title,
                        scheduledAtEpochMs = entity.scheduledAtEpochMs,
                        gestationalWeekAtVisit = status.displayWeekNumber,
                    )
                }
            return PregnancyPlanTimelineMerger
                .merge(cachedCarePlan, appointmentRows)
                .map { entry ->
                    when (entry) {
                        is PregnancyPlanTimelineMerger.TimelineEntry.CarePlan -> {
                            val row = entry.row
                            PregnancyPlanTimelineItemUi(
                                title = row.title,
                                subtitle =
                                    "هفتهٔ ${PregnancyFormatters.formatInteger(row.weekStart)}–" +
                                        PregnancyFormatters.formatInteger(row.weekEnd),
                                isCarePlan = true,
                                carePlanItemId = row.itemId,
                                carePlanDocumentId = row.documentId,
                            )
                        }
                        is PregnancyPlanTimelineMerger.TimelineEntry.Appointment -> {
                            val row = entry.row
                            val date =
                                Instant
                                    .ofEpochMilli(row.scheduledAtEpochMs)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            PregnancyPlanTimelineItemUi(
                                title = row.title,
                                subtitle = CycleFormatters.formatLocalDate(date),
                                isCarePlan = false,
                            )
                        }
                    }
                }
        }

        fun addCarePlanItemAsAppointment(item: PregnancyPlanTimelineItemUi) {
            if (!item.isCarePlan) return
            _uiState.update { it.copy(newAppointmentTitle = item.title) }
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
