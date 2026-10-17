package dev.mahin.android.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.android.navigation.MahinTopLevelDestination
import dev.mahin.core.analytics.AnalyticsEvent
import dev.mahin.core.analytics.AnalyticsGateway
import dev.mahin.core.common.LocalDateClock
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.content.ContentWarningSignsDto
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
    val usingCachedCard: Boolean = false,
    val failSafeNoCard: Boolean = false,
    val introTitle: String? = null,
    val introBody: String? = null,
    val signs: List<PregnancyWarningSignItemUi> = emptyList(),
    val escalationTitle: String? = null,
    val escalationBody: String? = null,
    val contactClinicianLabel: String? = null,
    val bpFlagLevel: PregnancyBpFlagLevel = PregnancyBpFlagLevel.NONE,
    val bpFlagMessage: String? = null,
    val loggedBpLabel: String? = null,
)

@HiltViewModel
class PregnancyWarningSignsViewModel
    @Inject
    constructor(
        private val contentRepository: ContentRepository,
        private val trackingRepository: PregnancyTrackingRepository,
        private val localDateClock: LocalDateClock,
        private val analytics: AnalyticsGateway,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PregnancyWarningSignsUiState())
        val uiState: StateFlow<PregnancyWarningSignsUiState> = _uiState.asStateFlow()

        init {
            analytics.track(
                AnalyticsEvent(
                    "foundation_opened",
                    mapOf("surface" to MahinTopLevelDestination.PREGNANCY_WARNING_SIGNS_ROUTE),
                ),
            )
            viewModelScope.launch { load() }
        }

        private suspend fun load() {
            val fetch = contentRepository.warningSignsCardFetch()
            val pregnancy = trackingRepository.observeActivePregnancy().first()
            val today = localDateClock.today()
            val log = pregnancy?.let { trackingRepository.getPregnancyDayLogForDate(today) }
            val reading =
                log?.bpSystolic?.let { sys ->
                    log.bpDiastolic?.let { dia -> PregnancyBpReading(sys, dia) }
                }
            val loggedBpLabel =
                reading?.let { bp ->
                    PregnancyFormatters.formatInteger(bp.systolic) +
                        "/" +
                        PregnancyFormatters.formatInteger(bp.diastolic)
                }
            if (fetch == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        failSafeNoCard = true,
                        usingCachedCard = false,
                        bpFlagLevel = PregnancyBpFlagLevel.NONE,
                        bpFlagMessage = null,
                        loggedBpLabel = loggedBpLabel,
                    )
                }
                return
            }
            val card =
                when (fetch) {
                    is ContentRepository.WarningSignsCardFetch.Network -> fetch.card
                    is ContentRepository.WarningSignsCardFetch.Cache -> fetch.card
                }
            val thresholds =
                PregnancyBpThresholds(
                    elevatedSystolic = card.bpThresholds.elevatedSystolic,
                    elevatedDiastolic = card.bpThresholds.elevatedDiastolic,
                    urgentSystolic = card.bpThresholds.urgentSystolic,
                    urgentDiastolic = card.bpThresholds.urgentDiastolic,
                )
            val flagLevel =
                pregnancy?.let {
                    PregnancyBpFlagEvaluator.evaluate(reading, thresholds)
                } ?: PregnancyBpFlagLevel.NONE
            _uiState.update {
                it.copy(
                    isLoading = false,
                    failSafeNoCard = false,
                    usingCachedCard = fetch is ContentRepository.WarningSignsCardFetch.Cache,
                    introTitle = card.introTitle,
                    introBody = card.introBody,
                    signs = card.signs.map { sign -> PregnancyWarningSignItemUi(sign.title, sign.summary) },
                    escalationTitle = card.escalationTitleFor(flagLevel),
                    escalationBody = card.escalationBodyFor(flagLevel),
                    contactClinicianLabel = card.contactClinicianLabel,
                    bpFlagLevel = flagLevel,
                    bpFlagMessage =
                        when (flagLevel) {
                            PregnancyBpFlagLevel.NONE -> null
                            PregnancyBpFlagLevel.ELEVATED -> card.contactClinicianLabel
                            PregnancyBpFlagLevel.URGENT -> card.urgentContactClinicianLabel
                        },
                    loggedBpLabel = loggedBpLabel,
                )
            }
        }
    }

private fun ContentWarningSignsDto.escalationTitleFor(flagLevel: PregnancyBpFlagLevel): String =
    if (flagLevel == PregnancyBpFlagLevel.URGENT) urgentEscalationTitle else escalationTitle

private fun ContentWarningSignsDto.escalationBodyFor(flagLevel: PregnancyBpFlagLevel): String =
    if (flagLevel == PregnancyBpFlagLevel.URGENT) urgentEscalationBody else escalationBody
