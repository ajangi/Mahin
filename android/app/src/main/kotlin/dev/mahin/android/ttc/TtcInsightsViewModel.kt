package dev.mahin.android.ttc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.entity.TtcDayLogEntity
import dev.mahin.core.database.ttc.TtcTrackingRepository
import dev.mahin.core.datastore.TtcPrivacyPreferencesRepository
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.fertility.FertilityInsightEngineV1
import dev.mahin.domain.fertility.FertilityInsightResult
import dev.mahin.domain.fertility.TtcSignalDay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TtcInsightsUiState(
    val isLoading: Boolean = true,
    val isTtcMode: Boolean = false,
    val intercourseLoggingEnabled: Boolean = false,
    val insight: FertilityInsightResult? = null,
    val timeline: List<TtcDayLogEntity> = emptyList(),
    val bbtPoints: List<BbtChartPoint> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TtcInsightsViewModel
    @Inject
    constructor(
        private val cycleRepository: CycleTrackingRepository,
        private val ttcRepository: TtcTrackingRepository,
        private val ttcPrivacyRepository: TtcPrivacyPreferencesRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(TtcInsightsUiState())
        val uiState: StateFlow<TtcInsightsUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                calendarDayTicker()
                    .flatMapLatest { today ->
                        val windowStart = today.minusDays(35)
                        combine(
                            cycleRepository.observeDashboard(today),
                            ttcRepository.observeTtcLogs(windowStart, today),
                            ttcRepository.observeProfile(),
                            ttcPrivacyRepository.observeIntercourseLoggingEnabled(),
                        ) { dashboard, logs, profile, intercourseEnabled ->
                            buildState(today, dashboard, logs, profile, intercourseEnabled)
                        }
                    }.collect { state ->
                        _uiState.update { state }
                    }
            }
        }

        private fun buildState(
            today: LocalDate,
            dashboard: dev.mahin.core.database.cycle.CycleDashboard,
            logs: List<TtcDayLogEntity>,
            profile: dev.mahin.core.database.entity.CycleProfileEntity?,
            intercourseEnabled: Boolean,
        ): TtcInsightsUiState {
            val isTtc = profile?.reproductiveMode == ReproductiveMode.TRYING_TO_CONCEIVE
            if (!isTtc) {
                return TtcInsightsUiState(
                    isLoading = false,
                    isTtcMode = false,
                    intercourseLoggingEnabled = intercourseEnabled,
                )
            }
            val cycleStart =
                FertilityInsightEngineV1.currentCycleStart(
                    prediction = dashboard.prediction,
                    today = today,
                    periodAnchorStart = dashboard.openPeriodStart,
                )
            val signals =
                logs.map { entry ->
                    TtcSignalDay(
                        date = entry.logDate,
                        bbtCelsius = entry.bbtCelsius,
                        ovulationTestResult = entry.ovulationTestResult,
                        cervicalMucus = entry.cervicalMucus,
                        intercourseLogged = entry.intercourseLogged,
                        pregnancyTestResult = entry.pregnancyTestResult,
                    )
                }
            val insight =
                FertilityInsightEngineV1.buildInsight(
                    prediction = dashboard.prediction,
                    signals = signals,
                    currentCycleStart = cycleStart,
                )
            val bbtPoints =
                logs.mapNotNull { entry ->
                    entry.bbtCelsius?.let { BbtChartPoint(entry.logDate, it) }
                }
            return TtcInsightsUiState(
                isLoading = false,
                isTtcMode = true,
                intercourseLoggingEnabled = intercourseEnabled,
                insight = insight,
                timeline = logs.sortedByDescending { it.logDate },
                bbtPoints = bbtPoints,
            )
        }

        private fun calendarDayTicker(): Flow<LocalDate> =
            flow {
                while (true) {
                    emit(LocalDate.now())
                    val now = LocalDateTime.now()
                    val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
                    val delayMs = Duration.between(now, nextMidnight).toMillis().coerceAtLeast(60_000L)
                    delay(delayMs)
                }
            }
    }
