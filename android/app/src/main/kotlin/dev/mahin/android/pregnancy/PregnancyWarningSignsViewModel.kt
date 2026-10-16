package dev.mahin.android.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.common.LocalDateClock
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.domain.pregnancy.PregnancyBpFlagEvaluator
import dev.mahin.domain.pregnancy.PregnancyBpFlagLevel
import dev.mahin.domain.pregnancy.PregnancyBpReading
import dev.mahin.domain.pregnancy.PregnancyBpThresholds
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PregnancyWarningSignItemUi(
    val title: String,
    val summary: String,
)

data class PregnancyWarningSignsUiState(
    val isLoading: Boolean = true,
    val offline: Boolean = false,
    val introTitle: String? = null,
    val introBody: String? = null,
    val signs: List<PregnancyWarningSignItemUi> = emptyList(),
    val escalationTitle: String? = null,
    val escalationBody: String? = null,
    val contactClinicianLabel: String? = null,
    val bpFlagLevel: PregnancyBpFlagLevel = PregnancyBpFlagLevel.NONE,
    val bpFlagMessage: String? = null,
)

@HiltViewModel
class PregnancyWarningSignsViewModel
    @Inject
    constructor(
        private val contentRepository: ContentRepository,
        private val trackingRepository: PregnancyTrackingRepository,
        private val localDateClock: LocalDateClock,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PregnancyWarningSignsUiState())
        val uiState: StateFlow<PregnancyWarningSignsUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                val card = contentRepository.warningSignsCard()
                if (card == null) {
                    _uiState.update { it.copy(isLoading = false, offline = true) }
                    return@launch
                }
                val thresholds =
                    PregnancyBpThresholds(
                        elevatedSystolic = card.bpThresholds.elevatedSystolic,
                        elevatedDiastolic = card.bpThresholds.elevatedDiastolic,
                        urgentSystolic = card.bpThresholds.urgentSystolic,
                        urgentDiastolic = card.bpThresholds.urgentDiastolic,
                    )
                val pregnancy = trackingRepository.observeActivePregnancy().first()
                val flagLevel =
                    pregnancy?.let {
                        val today = localDateClock.today()
                        val log = trackingRepository.getPregnancyDayLogForDate(today)
                        val reading =
                            log?.bpSystolic?.let { sys ->
                                log.bpDiastolic?.let { dia ->
                                    PregnancyBpReading(sys, dia)
                                }
                            }
                        PregnancyBpFlagEvaluator.evaluate(reading, thresholds)
                    } ?: PregnancyBpFlagLevel.NONE
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        offline = false,
                        introTitle = card.introTitle,
                        introBody = card.introBody,
                        signs = card.signs.map { sign -> PregnancyWarningSignItemUi(sign.title, sign.summary) },
                        escalationTitle = card.escalationTitle,
                        escalationBody = card.escalationBody,
                        contactClinicianLabel = card.contactClinicianLabel,
                        bpFlagLevel = flagLevel,
                        bpFlagMessage =
                            when (flagLevel) {
                                PregnancyBpFlagLevel.NONE -> null
                                PregnancyBpFlagLevel.ELEVATED ->
                                    card.contactClinicianLabel
                                PregnancyBpFlagLevel.URGENT ->
                                    card.contactClinicianLabel
                            },
                    )
                }
            }
        }
    }
