package dev.mahin.android.ttc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.ttc.TtcTrackingRepository
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.fertility.FertilityInsightEngineV1
import dev.mahin.domain.fertility.FertilityInsightResult
import dev.mahin.domain.fertility.TtcSignalDay
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TtcInsightsUiState(
    val insight: FertilityInsightResult? = null,
    val timeline: List<dev.mahin.core.database.entity.TtcDayLogEntity> = emptyList(),
    val bbtPoints: List<BbtChartPoint> = emptyList(),
)

@HiltViewModel
class TtcInsightsViewModel
    @Inject
    constructor(
        private val cycleRepository: CycleTrackingRepository,
        private val ttcRepository: TtcTrackingRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(TtcInsightsUiState())
        val uiState: StateFlow<TtcInsightsUiState> = _uiState.asStateFlow()

        init {
            val today = LocalDate.now()
            val windowStart = today.minusDays(35)
            viewModelScope.launch {
                combine(
                    cycleRepository.observeDashboard(today),
                    ttcRepository.observeTtcLogs(windowStart, today),
                    ttcRepository.observeProfile(),
                ) { dashboard, logs, profile ->
                    val isTtc = profile?.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE
                    if (!isTtc) {
                        TtcInsightsUiState()
                    } else {
                        val signals =
                            logs.map { entry ->
                                TtcSignalDay(
                                    date = entry.logDate,
                                    bbtCelsius = entry.bbtCelsius,
                                    ovulationTestResult = entry.ovulationTestResult?.name,
                                    cervicalMucus = entry.cervicalMucus?.name,
                                    intercourseLogged = entry.intercourseLogged,
                                    pregnancyTestResult = entry.pregnancyTestResult?.name,
                                )
                            }
                        val insight =
                            FertilityInsightEngineV1.buildInsight(
                                prediction = dashboard.prediction,
                                signals = signals,
                                today = today,
                            )
                        val bbtPoints =
                            logs.mapNotNull { entry ->
                                entry.bbtCelsius?.let { BbtChartPoint(entry.logDate, it) }
                            }
                        TtcInsightsUiState(
                            insight = insight,
                            timeline = logs.sortedByDescending { it.logDate },
                            bbtPoints = bbtPoints,
                        )
                    }
                }.collect { state ->
                    _uiState.update { state }
                }
            }
        }
    }
