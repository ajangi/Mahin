package dev.mahin.android.cycle.editor

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.model.LogEditorCategory
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch

@Composable
fun DayEditorSexualOptInDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.day_editor_sexual_opt_in_title)) },
        text = { Text(stringResource(R.string.day_editor_sexual_opt_in_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.testTag("day_editor_sexual_opt_in_confirm")) {
                Text(stringResource(R.string.day_editor_sexual_opt_in_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.day_editor_sexual_opt_in_cancel))
            }
        },
        modifier = Modifier.testTag("day_editor_sexual_opt_in"),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEditorManageCategoriesSheet(
    state: DayEditorUiState,
    onDismiss: () -> Unit,
    onLifestyleToggle: (LogEditorCategory, Boolean) -> Unit,
    onRequestSexualOptIn: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, modifier = Modifier.testTag("day_editor_manage_categories_sheet")) {
        DayEditorManageCategoriesPanel(
            state = state,
            onLifestyleToggle = onLifestyleToggle,
            onRequestSexualOptIn = onRequestSexualOptIn,
        )
    }
}

@Composable
internal fun DayEditorManageCategoriesPanel(
    state: DayEditorUiState,
    onLifestyleToggle: (LogEditorCategory, Boolean) -> Unit,
    onRequestSexualOptIn: () -> Unit,
) {
    Column(modifier = Modifier.padding(MahinSpacing.lg)) {
        Text(text = stringResource(R.string.day_editor_manage_categories))
        LifestyleRow(
                label = stringResource(R.string.day_editor_lifestyle_sleep),
                enabled = state.lifestyleEnabled[LogEditorCategory.SLEEP] == true,
                onToggle = { onLifestyleToggle(LogEditorCategory.SLEEP, it) },
                testTag = "day_editor_lifestyle_sleep",
            )
            LifestyleRow(
                label = stringResource(R.string.day_editor_lifestyle_exercise),
                enabled = state.lifestyleEnabled[LogEditorCategory.EXERCISE] == true,
                onToggle = { onLifestyleToggle(LogEditorCategory.EXERCISE, it) },
                testTag = "day_editor_lifestyle_exercise",
            )
            LifestyleRow(
                label = stringResource(R.string.day_editor_lifestyle_water),
                enabled = state.lifestyleEnabled[LogEditorCategory.WATER] == true,
                onToggle = { onLifestyleToggle(LogEditorCategory.WATER, it) },
                testTag = "day_editor_lifestyle_water",
            )
            LifestyleRow(
                label = stringResource(R.string.day_editor_lifestyle_stress),
                enabled = state.lifestyleEnabled[LogEditorCategory.STRESS] == true,
                onToggle = { onLifestyleToggle(LogEditorCategory.STRESS, it) },
                testTag = "day_editor_lifestyle_stress",
            )
        if (!state.sexualActivityEnabled) {
            TextButton(onClick = onRequestSexualOptIn) {
                Text(stringResource(R.string.day_editor_enable_sexual_activity))
            }
        }
    }
}

@Composable
private fun LifestyleRow(
    label: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    testTag: String,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.testTag(testTag),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}
