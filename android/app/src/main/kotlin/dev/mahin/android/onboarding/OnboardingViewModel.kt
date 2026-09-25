package dev.mahin.android.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mahin.core.database.cycle.CycleOnboardingInput
import dev.mahin.core.database.cycle.CycleTrackingRepository
import dev.mahin.core.database.pregnancy.PregnancyTrackingRepository
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val repository: CycleTrackingRepository,
        private val pregnancyRepository: PregnancyTrackingRepository,
    ) : ViewModel() {
        private val _saving = MutableStateFlow(false)
        val saving: StateFlow<Boolean> = _saving.asStateFlow()

        fun finishOnboarding(
            input: CycleOnboardingInput,
            onComplete: () -> Unit,
        ) {
            viewModelScope.launch {
                _saving.value = true
                repository.completeOnboarding(input)
                _saving.value = false
                onComplete()
            }
        }

        fun finishPregnancyOnboarding(
            lmpDate: LocalDate,
            clinicalEddDate: LocalDate?,
            onComplete: () -> Unit,
        ) {
            viewModelScope.launch {
                _saving.value = true
                repository.completeOnboarding(
                    CycleOnboardingInput(
                        mode = ReproductiveMode.PREGNANT,
                        lastPeriodStart = lmpDate,
                        lastPeriodEnd = null,
                        typicalCycleLengthDays = null,
                        typicalPeriodLengthDays = null,
                        regularity = dev.mahin.core.model.CycleRegularity.UNKNOWN,
                    ),
                )
                pregnancyRepository.startPregnancy(
                    lmpDate = lmpDate,
                    clinicalEddDate = clinicalEddDate,
                    datingReason = null,
                )
                _saving.value = false
                onComplete()
            }
        }
    }
