package dev.mahin.android.golden

import dev.mahin.android.cycle.editor.DayEditorUiState
import dev.mahin.core.model.LogEditorCategory
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PainLocation
import dev.mahin.core.model.PeriodFlowLevel
import dev.mahin.core.model.PregnancyTestResult
import dev.mahin.core.model.ReproductiveMode
import java.time.LocalDate

object M16GoldenFixtures {
    fun cycleEditor(): DayEditorUiState =
        DayEditorUiState(
            date = LocalDate.of(2025, 3, 15),
            reproductiveMode = ReproductiveMode.CYCLE_TRACKING,
            orderedCategories =
                listOf(
                    LogEditorCategory.PERIOD_FLOW,
                    LogEditorCategory.SYMPTOMS,
                    LogEditorCategory.MOOD,
                    LogEditorCategory.PAIN,
                ),
            periodLogged = true,
            flowLevel = PeriodFlowLevel.MEDIUM,
            symptomTags = setOf("خستگی"),
            moodTags = setOf("آرام"),
            painLocation = PainLocation.ABDOMEN,
            painSeverity = 4,
        )

    fun ttcEditor(): DayEditorUiState =
        cycleEditor().copy(
            reproductiveMode = ReproductiveMode.TRYING_TO_CONCEIVE,
            orderedCategories =
                listOf(
                    LogEditorCategory.TTC_TESTS,
                    LogEditorCategory.DISCHARGE,
                    LogEditorCategory.PERIOD_FLOW,
                    LogEditorCategory.SYMPTOMS,
                    LogEditorCategory.MOOD,
                    LogEditorCategory.PAIN,
                ),
            bbtInput = "36.6",
            ovulationTest = OvulationTestResult.POSITIVE,
            pregnancyTest = PregnancyTestResult.NEGATIVE,
        )

    fun pregnancyEditor(): DayEditorUiState =
        DayEditorUiState(
            date = LocalDate.of(2025, 3, 15),
            reproductiveMode = ReproductiveMode.PREGNANT,
            orderedCategories =
                listOf(
                    LogEditorCategory.PREGNANCY_SYMPTOMS,
                    LogEditorCategory.PREGNANCY_VITALS,
                    LogEditorCategory.MOOD,
                    LogEditorCategory.NOTES,
                ),
            pregnancySymptomTags = setOf("تهوع"),
            moodTags = setOf("آرام"),
            note = "یادداشت",
        )

    fun sexualOptIn(): DayEditorUiState =
        DayEditorUiState(
            showSexualOptIn = true,
            date = LocalDate.of(2025, 3, 15),
        )

    fun manageCategories(): DayEditorUiState =
        DayEditorUiState(
            showManageCategories = true,
            date = LocalDate.of(2025, 3, 15),
            lifestyleEnabled = mapOf(LogEditorCategory.SLEEP to true),
        )
}
