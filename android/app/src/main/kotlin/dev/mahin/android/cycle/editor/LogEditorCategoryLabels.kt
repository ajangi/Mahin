package dev.mahin.android.cycle.editor

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.model.LogEditorCategory

private val logEditorCategoryLabelRes: Map<LogEditorCategory, Int> =
    mapOf(
        LogEditorCategory.PERIOD_FLOW to R.string.log_editor_category_period_flow,
        LogEditorCategory.SYMPTOMS to R.string.log_editor_category_symptoms,
        LogEditorCategory.MOOD to R.string.log_editor_category_mood,
        LogEditorCategory.PAIN to R.string.log_editor_category_pain,
        LogEditorCategory.DISCHARGE to R.string.log_editor_category_discharge,
        LogEditorCategory.TTC_TESTS to R.string.log_editor_category_ttc_tests,
        LogEditorCategory.MEDICATION to R.string.log_editor_category_medication,
        LogEditorCategory.NOTES to R.string.log_editor_category_notes,
        LogEditorCategory.SEXUAL_ACTIVITY to R.string.log_editor_category_sexual_activity,
        LogEditorCategory.SLEEP to R.string.log_editor_category_sleep,
        LogEditorCategory.EXERCISE to R.string.log_editor_category_exercise,
        LogEditorCategory.WATER to R.string.log_editor_category_water,
        LogEditorCategory.STRESS to R.string.log_editor_category_stress,
        LogEditorCategory.PREGNANCY_SYMPTOMS to R.string.log_editor_category_pregnancy_symptoms,
        LogEditorCategory.PREGNANCY_VITALS to R.string.log_editor_category_pregnancy_vitals,
    )

@StringRes
fun LogEditorCategory.labelRes(): Int = logEditorCategoryLabelRes.getValue(this)

@Composable
fun LogEditorCategory.localizedLabel(): String = stringResource(labelRes())
