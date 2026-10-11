package dev.mahin.android.golden

import dev.mahin.android.cycle.editor.DayEditorUiState
import dev.mahin.core.model.LogEditorCategory
import dev.mahin.core.model.PainLocation
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
            symptomTags = setOf("خستگی"),
            moodTags = setOf("آرام"),
            painLocation = PainLocation.ABDOMEN,
            painSeverity = 4,
        )

    fun ttcEditor(): DayEditorUiState =
        cycleEditor().copy(
            reproductiveMode = ReproductiveMode.TRYING_TO_CONCEIVE,
            orderedCategories = cycleEditor().orderedCategories + LogEditorCategory.TTC_TESTS,
        )

    fun pregnancyEditor(): DayEditorUiState =
        cycleEditor().copy(
            reproductiveMode = ReproductiveMode.PREGNANT,
            orderedCategories =
                listOf(
                    LogEditorCategory.PREGNANCY_SYMPTOMS,
                    LogEditorCategory.PREGNANCY_VITALS,
                    LogEditorCategory.NOTES,
                ),
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
