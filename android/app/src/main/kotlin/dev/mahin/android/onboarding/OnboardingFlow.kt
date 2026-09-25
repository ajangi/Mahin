package dev.mahin.android.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.core.database.cycle.CycleOnboardingInput
import dev.mahin.core.datetime.JalaliDate
import dev.mahin.core.datetime.PersianCivilDateConverter
import dev.mahin.core.datetime.PregnancyClinicalEddInput
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.model.CycleRegularity
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate

@Composable
@Suppress("LongMethod")
fun OnboardingFlow(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    var step by remember { mutableIntStateOf(0) }
    var selectedMode by remember { mutableStateOf(ReproductiveMode.CYCLE_TRACKING) }
    var lastPeriodStart by remember { mutableStateOf(LocalDate.now().minusDays(7)) }
    var lastPeriodEnd by remember { mutableStateOf<LocalDate?>(null) }
    var cycleLength by remember { mutableStateOf<Int?>(28) }
    var periodLength by remember { mutableStateOf<Int?>(5) }
    var regularity by remember { mutableStateOf(CycleRegularity.UNKNOWN) }
    var lmpJalali by remember { mutableStateOf(PersianCivilDateConverter.toJalali(LocalDate.now().minusWeeks(8))) }
    var includeClinicalEdd by remember { mutableStateOf(false) }
    var clinicalEddJalali by remember { mutableStateOf<JalaliDate?>(null) }
    val saving by viewModel.saving.collectAsStateWithLifecycle()

    Scaffold { padding ->
        when (step) {
            0 ->
                OnboardingWelcomeScreen(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(MahinSpacing.md),
                    onContinue = { step = 1 },
                )
            1 ->
                OnboardingGoalScreen(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(MahinSpacing.md),
                    selectedMode = selectedMode,
                    onModeSelected = { selectedMode = it },
                    onContinue = { step = 2 },
                )
            else ->
                if (selectedMode == ReproductiveMode.PREGNANT) {
                    OnboardingPregnancySetupScreen(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .verticalScroll(rememberScrollState())
                                .padding(MahinSpacing.md),
                        form =
                            OnboardingPregnancyFormState(
                                lmpJalali = lmpJalali,
                                includeClinicalEdd = includeClinicalEdd,
                                clinicalEddJalali = clinicalEddJalali,
                                isSaving = saving,
                            ),
                        onLmpChange = { lmpJalali = it },
                        onIncludeClinicalToggle = {
                            if (!includeClinicalEdd) {
                                clinicalEddJalali = PregnancyClinicalEddInput.defaultClinicalEddJalali()
                            }
                            includeClinicalEdd = !includeClinicalEdd
                        },
                        onClinicalEddChange = { clinicalEddJalali = it },
                        onFinish = {
                            val lmp = PersianCivilDateConverter.toGregorian(lmpJalali)
                            val clinical =
                                PregnancyClinicalEddInput.resolveClinicalEddGregorian(
                                    includeClinical = includeClinicalEdd,
                                    selectedClinicalJalali = clinicalEddJalali,
                                )
                            viewModel.finishPregnancyOnboarding(
                                lmpDate = lmp,
                                clinicalEddDate = clinical,
                                onComplete = onComplete,
                            )
                        },
                    )
                } else {
                    OnboardingCycleSetupScreen(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .verticalScroll(rememberScrollState())
                                .padding(MahinSpacing.md),
                        form =
                            OnboardingSetupFormState(
                                mode = selectedMode,
                                lastPeriodStart = lastPeriodStart,
                                typicalCycleLength = cycleLength,
                                typicalPeriodLength = periodLength,
                                regularity = regularity,
                                isSaving = saving,
                            ),
                        onLastPeriodStartChange = { lastPeriodStart = it },
                        onCycleLengthChange = { cycleLength = it },
                        onPeriodLengthChange = { periodLength = it },
                        onRegularityChange = { regularity = it },
                        onFinish = {
                            viewModel.finishOnboarding(
                                input =
                                    CycleOnboardingInput(
                                        mode = selectedMode,
                                        lastPeriodStart = lastPeriodStart,
                                        lastPeriodEnd = lastPeriodEnd,
                                        typicalCycleLengthDays = cycleLength,
                                        typicalPeriodLengthDays = periodLength,
                                        regularity = regularity,
                                    ),
                                onComplete = onComplete,
                            )
                        },
                    )
                }
        }
    }
}
