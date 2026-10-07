package dev.mahin.android.cycle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.database.cycle.CycleDashboard
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.pregnancy.PregnancyDatingEngineV1
import dev.mahin.domain.pregnancy.PregnancyStatusSnapshot
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TodayUiState(
    val dashboard: CycleDashboard? = null,
    val reproductiveMode: ReproductiveMode = ReproductiveMode.CYCLE_TRACKING,
    val pregnancyStatus: PregnancyStatusSnapshot? = null,
    val postPregnancyTransition: Boolean = false,
)

@HiltViewModel
class TodayViewModel
    @Inject
    constructor(
        repository: CycleTrackingRepository,
        private val pregnancyRepository: PregnancyTrackingRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(TodayUiState())
        val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                repository.observeDashboard().collect { dashboard ->
                    _uiState.update { it.copy(dashboard = dashboard) }
                }
            }
            viewModelScope.launch {
                combine(
                    pregnancyRepository.observeProfile(),
                    pregnancyRepository.observeActivePregnancy(),
                ) { profile, pregnancy ->
                    profile to pregnancy
                }.collect { (profile, pregnancy) ->
                    val mode = profile?.reproductiveMode ?: ReproductiveMode.CYCLE_TRACKING
                    val status =
                        pregnancy?.let {
                            PregnancyDatingEngineV1.status(
                                lmpDate = it.lmpDate,
                                clinicalEddDate = it.clinicalEddDate,
                                asOfDate = LocalDate.now(),
                            )
                        }
                    _uiState.update {
                        it.copy(
                            reproductiveMode = mode,
                            pregnancyStatus = if (mode == ReproductiveMode.PREGNANT) status else null,
                            postPregnancyTransition = mode == ReproductiveMode.POST_PREGNANCY_TRANSITION,
                        )
                    }
                }
            }
        }

        fun resumeCycleTracking() {
            viewModelScope.launch {
                pregnancyRepository.resumeTracking(ReproductiveMode.CYCLE_TRACKING)
            }
        }

        fun resumeTtc() {
            viewModelScope.launch {
                pregnancyRepository.resumeTracking(ReproductiveMode.TRYING_TO_CONCEIVE)
            }
        }
    }
