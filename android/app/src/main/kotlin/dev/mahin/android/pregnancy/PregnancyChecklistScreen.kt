package dev.mahin.android.pregnancy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.component.MahinEmptyState
import dev.mahin.core.designsystem.component.MahinPrimaryButton
import dev.mahin.core.designsystem.component.MahinScreenHeader

data class PregnancyChecklistItemUi(
    val id: String,
    val section: String,
    val label: String,
    val checked: Boolean,
)

data class PregnancyChecklistUiState(
    val isLoading: Boolean = true,
    val items: List<PregnancyChecklistItemUi> = emptyList(),
    val progressLabel: String = "",
)

@Composable
fun PregnancyChecklistScreen(
    modifier: Modifier = Modifier,
    viewModel: PregnancyChecklistViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PregnancyChecklistScreenContent(
        state = state,
        onToggle = viewModel::toggleItem,
        onAddItem = viewModel::addItem,
        onRenameItem = viewModel::renameItem,
        onDeleteItem = viewModel::deleteItem,
        modifier = modifier,
    )
}

@Composable
@Suppress("LongParameterList")
fun PregnancyChecklistScreenContent(
    state: PregnancyChecklistUiState,
    onToggle: (String, Boolean) -> Unit,
    onAddItem: (String) -> Unit,
    onRenameItem: (String, String) -> Unit,
    onDeleteItem: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var newItemLabel by remember { mutableStateOf("") }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("pregnancy_checklist")
                .padding(MahinSpacing.md),
    ) {
        MahinScreenHeader(
            title = stringResource(R.string.pregnancy_checklist_title),
            subtitle = stringResource(R.string.pregnancy_checklist_subtitle),
        )
        if (!state.isLoading && state.items.isNotEmpty()) {
            Text(
                text = state.progressLabel,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = MahinSpacing.sm),
            )
        }
        when {
            state.isLoading -> Unit
            state.items.isEmpty() -> {
                MahinEmptyState(
                    title = stringResource(R.string.pregnancy_checklist_empty_title),
                    body = stringResource(R.string.pregnancy_checklist_empty_body),
                )
            }
            else -> {
                LazyColumn(modifier = Modifier.testTag("pregnancy_checklist_list")) {
                    items(state.items, key = { it.id }) { item ->
                        ChecklistRow(
                            item = item,
                            onToggle = onToggle,
                            onRename = onRenameItem,
                            onDelete = onDeleteItem,
                        )
                    }
                }
                OutlinedTextField(
                    value = newItemLabel,
                    onValueChange = { newItemLabel = it },
                    label = { Text(stringResource(R.string.pregnancy_checklist_new_item_label)) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = MahinSpacing.sm)
                            .testTag("pregnancy_checklist_new_item_field"),
                )
                MahinPrimaryButton(
                    text = stringResource(R.string.pregnancy_checklist_add_item),
                    onClick = {
                        onAddItem(newItemLabel)
                        newItemLabel = ""
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag("pregnancy_checklist_add_item_button"),
                )
            }
        }
    }
}

@Composable
private fun ChecklistRow(
    item: PregnancyChecklistItemUi,
    onToggle: (String, Boolean) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var editing by remember(item.id) { mutableStateOf(false) }
    var draftLabel by remember(item.id) { mutableStateOf(item.label) }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = MahinSpacing.xs)) {
        Checkbox(
            checked = item.checked,
            onCheckedChange = { onToggle(item.id, it) },
            modifier = Modifier.testTag("pregnancy_checklist_item_${item.id}"),
        )
        if (editing) {
            OutlinedTextField(
                value = draftLabel,
                onValueChange = { draftLabel = it },
                modifier = Modifier.testTag("pregnancy_checklist_rename_${item.id}"),
            )
            TextButton(
                onClick = {
                    onRename(item.id, draftLabel)
                    editing = false
                },
            ) {
                Text(stringResource(R.string.pregnancy_checklist_save_rename))
            }
        } else {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("pregnancy_checklist_label_${item.id}"),
            )
            TextButton(onClick = { editing = true }) {
                Text(stringResource(R.string.pregnancy_checklist_rename))
            }
        }
        Text(
            text = item.section,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(
            onClick = { onDelete(item.id) },
            modifier = Modifier.testTag("pregnancy_checklist_delete_${item.id}"),
        ) {
            Text(stringResource(R.string.pregnancy_checklist_delete))
        }
    }
}
