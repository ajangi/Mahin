package dev.mahin.android.pregnancy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.common.LocalDateClock
import dev.mahin.core.content.ContentRepository
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.model.ReproductiveMode
import dev.mahin.domain.pregnancy.PregnancyDatingEngineV1
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PregnancyWeekByWeekViewModel
    @Inject
    constructor(
        private val pregnancyRepository: PregnancyTrackingRepository,
        private val contentRepository: ContentRepository,
        private val localDateClock: LocalDateClock,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(PregnancyWeekByWeekUiState())
        val uiState: StateFlow<PregnancyWeekByWeekUiState> = _uiState.asStateFlow()

        private val loadedWeeks = mutableMapOf<Int, PregnancyWeekPageState>()

        init {
            viewModelScope.launch {
                kotlinx.coroutines.flow.combine(
                    pregnancyRepository.observeProfile(),
                    pregnancyRepository.observeActivePregnancy(),
                ) { profile, record ->
                    val pregnant = profile?.reproductiveMode == ReproductiveMode.PREGNANT
                    val week =
                        if (!pregnant || record == null) {
                            1
                        } else {
                            PregnancyDatingEngineV1.status(
                                lmpDate = record.lmpDate,
                                clinicalEddDate = record.clinicalEddDate,
                                asOfDate = localDateClock.today(),
                            ).displayWeekNumber
                        }
                    week
                }.collect { week ->
                    _uiState.update { it.copy(isLoading = false, currentWeek = week) }
                    onWeekSelected(week)
                }
            }
        }

        fun onWeekSelected(week: Int) {
            if (week in loadedWeeks) {
                _uiState.update {
                    it.copy(
                        currentWeek = week,
                        pages = (1..42).map { w -> loadedWeeks[w] ?: PregnancyWeekPageState(w, null, null, null, null, null) },
                    )
                }
                return
            }
            viewModelScope.launch {
                val article = contentRepository.pregnancyWeek(week)
                val page =
                    PregnancyWeekPageState(
                        weekNumber = week,
                        title = article?.title,
                        fetalSummary = article?.fetalSummary ?: article?.summary,
                        maternalSummary = article?.maternalSummary ?: article?.body,
                        sizeComparisonLabel = article?.sizeComparisonLabel,
                        illustrationAssetId = article?.illustrationMediaAssetId,
                    )
                loadedWeeks[week] = page
                _uiState.update {
                    it.copy(
                        currentWeek = week,
                        pages = (1..42).map { w -> loadedWeeks[w] ?: PregnancyWeekPageState(w, null, null, null, null, null) },
                    )
                }
            }
        }
    }
