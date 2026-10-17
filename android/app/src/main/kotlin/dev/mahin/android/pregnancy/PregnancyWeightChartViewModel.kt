package dev.mahin.android.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.android.navigation.MahinTopLevelDestination
import dev.mahin.core.analytics.AnalyticsEvent
import dev.mahin.core.analytics.AnalyticsGateway
import dev.mahin.core.common.LocalDateClock
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.domain.pregnancy.PregnancyDatingEngineV1
import dev.mahin.domain.pregnancy.PregnancyWeightChartModel
import dev.mahin.domain.pregnancy.PregnancyWeightChartPoint
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PregnancyWeightChartUiState(
    val isLoading: Boolean = true,
    val hasActivePregnancy: Boolean = false,
    val points: List<PregnancyWeightChartPoint> = emptyList(),
)

@HiltViewModel
class PregnancyWeightChartViewModel
    @Inject
    constructor(
        private val trackingRepository: PregnancyTrackingRepository,
        private val localDateClock: LocalDateClock,
        private val analytics: AnalyticsGateway,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PregnancyWeightChartUiState())
        val uiState: StateFlow<PregnancyWeightChartUiState> = _uiState.asStateFlow()

        init {
            analytics.track(
                AnalyticsEvent(
                    "foundation_opened",
                    mapOf("surface" to MahinTopLevelDestination.PREGNANCY_WEIGHT_CHART_ROUTE),
                ),
            )
            viewModelScope.launch {
                combine(
                    trackingRepository.observeActivePregnancy(),
                    trackingRepository.observePregnancyDayLogs(
                        LocalDate.of(2000, 1, 1),
                        localDateClock.today().plusYears(1),
                    ),
                ) { pregnancy, logs ->
                    if (pregnancy == null) {
                        PregnancyWeightChartUiState(isLoading = false, hasActivePregnancy = false)
                    } else {
                        val weekLogs =
                            logs.mapNotNull { log ->
                                val weight = log.weightKg ?: return@mapNotNull null
                                val week =
                                    PregnancyDatingEngineV1
                                        .status(
                                            lmpDate = pregnancy.lmpDate,
                                            clinicalEddDate = pregnancy.clinicalEddDate,
                                            asOfDate = log.logDate,
                                        ).displayWeekNumber
                                week to weight
                            }
                        PregnancyWeightChartUiState(
                            isLoading = false,
                            hasActivePregnancy = true,
                            points = PregnancyWeightChartModel.pointsFromLogs(weekLogs),
                        )
                    }
                }.collect { state -> _uiState.update { state } }
            }
        }
    }
