package dev.mahin.android.cycle.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.model.LogEditorCategory

@Composable
fun DayEditorSexualOptInDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.day_editor_sexual_opt_in_title)) },
        text = { DayEditorSexualOptInDialogBody() },
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

/** Golden-safe surface (no window/dialog root) matching [DayEditorSexualOptInDialog] copy. */
@Composable
fun DayEditorSexualOptInGoldenSurface(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(MahinSpacing.lg).testTag("day_editor_sexual_opt_in")) {
        Text(
            text = stringResource(R.string.day_editor_sexual_opt_in_title),
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
        )
        DayEditorSexualOptInDialogBody()
        Row(modifier = Modifier.padding(top = MahinSpacing.md)) {
            TextButton(onClick = {}, modifier = Modifier.testTag("day_editor_sexual_opt_in_confirm")) {
                Text(stringResource(R.string.day_editor_sexual_opt_in_confirm))
            }
            TextButton(onClick = {}) {
                Text(stringResource(R.string.day_editor_sexual_opt_in_cancel))
            }
        }
    }
}

@Composable
private fun DayEditorSexualOptInDialogBody() {
    Text(stringResource(R.string.day_editor_sexual_opt_in_body))
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
fun DayEditorManageCategoriesSheet(
    state: DayEditorUiState,
    onDismiss: () -> Unit,
    onLifestyleToggle: (LogEditorCategory, Boolean) -> Unit,
    onRequestSexualOptIn: () -> Unit,
    onMostUsedFirstToggle: (Boolean) -> Unit,
    onDisableSexualActivity: () -> Unit,
    onMoveCategoryEarlier: (LogEditorCategory) -> Unit,
    onMoveCategoryLater: (LogEditorCategory) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, modifier = Modifier.testTag("day_editor_manage_categories_sheet")) {
        DayEditorManageCategoriesPanel(
            state = state,
            onLifestyleToggle = onLifestyleToggle,
            onRequestSexualOptIn = onRequestSexualOptIn,
            onMostUsedFirstToggle = onMostUsedFirstToggle,
            onDisableSexualActivity = onDisableSexualActivity,
            onMoveCategoryEarlier = onMoveCategoryEarlier,
            onMoveCategoryLater = onMoveCategoryLater,
        )
    }
}

@Suppress("LongParameterList")
@Composable
internal fun DayEditorManageCategoriesPanel(
    state: DayEditorUiState,
    onLifestyleToggle: (LogEditorCategory, Boolean) -> Unit,
    onRequestSexualOptIn: () -> Unit,
    onMostUsedFirstToggle: (Boolean) -> Unit = {},
    onDisableSexualActivity: () -> Unit = {},
    onMoveCategoryEarlier: (LogEditorCategory) -> Unit = {},
    onMoveCategoryLater: (LogEditorCategory) -> Unit = {},
) {
    Column(modifier = Modifier.padding(MahinSpacing.lg)) {
        Text(text = stringResource(R.string.day_editor_manage_categories))
        LifestyleRow(
            label = stringResource(R.string.day_editor_most_used_first),
            enabled = state.mostUsedFirst,
            onToggle = onMostUsedFirstToggle,
            testTag = "day_editor_most_used_first",
        )
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
        if (state.sexualActivityEnabled) {
            TextButton(
                onClick = onDisableSexualActivity,
                modifier = Modifier.testTag("day_editor_disable_sexual_activity"),
            ) {
                Text(stringResource(R.string.day_editor_disable_sexual_activity))
            }
        } else {
            TextButton(onClick = onRequestSexualOptIn) {
                Text(stringResource(R.string.day_editor_enable_sexual_activity))
            }
        }
        state.orderedCategories.forEach { category ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("day_editor_category_order_${category.name}"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = category.name, modifier = Modifier.weight(1f))
                IconButton(onClick = { onMoveCategoryEarlier(category) }) {
                    Text("↑")
                }
                IconButton(onClick = { onMoveCategoryLater(category) }) {
                    Text("↓")
                }
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
    Row(
        modifier = Modifier.testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            modifier = Modifier.testTag("${testTag}_switch"),
        )
    }
}
